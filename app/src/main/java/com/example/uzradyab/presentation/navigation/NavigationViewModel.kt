package com.example.uzradyab.presentation.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.utils.FormatUtils
import com.example.uzradyab.domain.model.RouteDomainModel
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.NeshanRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import kotlin.math.roundToInt

/** Camera behaviour while navigating. */
enum class NavCamera {
    /** Heading-up, following the driver. */
    FOLLOW,
    /** North-up, fitted to the remaining route. */
    OVERVIEW,
    /** The driver moved the map by hand. */
    FREE,
}

/**
 * The screens of the routing flow (handoff section 1).
 * "Rotary" is not a separate phase: the banner switches to the roundabout layout whenever the
 * next maneuver is a rotary, while all live progress stays in [NavigationUiState].
 */
sealed interface NavigationPhase {
    data object Loading : NavigationPhase
    data class Failed(val message: String) : NavigationPhase
    data object Preview : NavigationPhase
    data class Steps(val fromNavigation: Boolean) : NavigationPhase
    data class Navigating(
        val camera: NavCamera = NavCamera.FOLLOW,
        val sheetOpen: Boolean = false,
        /** Full-screen list of the remaining steps, opened by tapping the banner. */
        val stepsOpen: Boolean = false,
    ) : NavigationPhase
    data object Rerouting : NavigationPhase
    data object Arrived : NavigationPhase
}

enum class LocationPermission { UNKNOWN, GRANTED, DENIED }

data class VehicleInfo(
    val name: String,
    val isStopped: Boolean,
    val lastUpdateMillis: Long?,
    val address: String?,
)

data class ArrivalSummary(
    val traveledMeters: Double,
    val tripSeconds: Double,
    val distanceToVehicle: Double,
    val sideText: String,
)

data class NavigationUiState(
    val phase: NavigationPhase = NavigationPhase.Loading,
    val permission: LocationPermission = LocationPermission.UNKNOWN,
    val userLocation: LatLng? = null,
    /** Direction of travel in degrees, from GPS or from the route. */
    val userBearing: Double? = null,
    val speedKmh: Int = 0,
    val destination: LatLng? = null,
    val deviceId: Long? = null,
    val vehicle: VehicleInfo? = null,
    val routes: List<RouteDomainModel> = emptyList(),
    val guidanceByRoute: List<List<GuidanceStep>> = emptyList(),
    val selectedRouteIndex: Int = 0,
    val progress: RouteProgress? = null,
    val traveledPoints: List<LatLng> = emptyList(),
    val remainingPoints: List<LatLng> = emptyList(),
    val focusedStepIndex: Int = 0,
    val voiceEnabled: Boolean = true,
    val arrival: ArrivalSummary? = null,
) {
    val selectedRoute: RouteDomainModel? get() = routes.getOrNull(selectedRouteIndex)
    val guidance: List<GuidanceStep> get() = guidanceByRoute.getOrNull(selectedRouteIndex).orEmpty()
    val fastestRouteIndex: Int get() = routes.indices.minByOrNull { routes[it].durationSeconds } ?: 0
    val vehicleName: String get() = vehicle?.name ?: "خودرو"
    /** "خودروی 114" */
    val destinationLabel: String get() = vehicle?.name?.let { "خودروی $it" } ?: "خودرو"
    val nextStep: GuidanceStep? get() = progress?.nextStepIndex?.let { guidance.getOrNull(it) }
    val followingStep: GuidanceStep? get() = progress?.nextStepIndex?.let { guidance.getOrNull(it + 1) }
}

@HiltViewModel
class NavigationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val neshanRepository: NeshanRepository,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    private var routeLine: RouteLine? = null
    private var hintAlong: Double? = null
    private var offRouteSince: Long? = null
    private var lastRerouteAt = 0L
    private var rerouteInFlight = false
    private var routesRequestInFlight = false
    private var tripStartedAt = 0L
    private var tripMeters = 0.0
    private var lastTripFix: LatLng? = null

    init {
        val lat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
        val lng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()
        val deviceId = savedStateHandle.get<Long>("deviceId")?.takeIf { it > 0 }

        if (lat != null && lng != null) {
            _uiState.update { it.copy(destination = LatLng(lat, lng), deviceId = deviceId) }
        } else {
            _uiState.update { it.copy(phase = NavigationPhase.Failed("موقعیت خودرو مشخص نیست.")) }
        }
        if (deviceId != null) loadVehicle(deviceId)
        // Location updates start only after the screen reports the permission result.
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permission = if (granted) LocationPermission.GRANTED else LocationPermission.DENIED) }
        if (granted) startLocationUpdates()
    }

    private fun loadVehicle(deviceId: Long) {
        viewModelScope.launch {
            val device = runCatching { deviceRepository.getDevice(deviceId) }.getOrNull()
            val position = runCatching { positionRepository.getLatestPosition(deviceId) }.getOrNull()
            val speedKmh = position?.let { (it.speed * 1.852).toInt() } ?: 0
            val lastUpdate = FormatUtils.parseIsoDate(device?.lastUpdate ?: position?.fixTime)?.time
            val info = VehicleInfo(
                name = device?.name ?: return@launch,
                isStopped = speedKmh <= 0,
                lastUpdateMillis = lastUpdate,
                address = position?.address?.takeIf { it.isNotBlank() },
            )
            _uiState.update { state ->
                val withVehicle = state.copy(vehicle = info)
                // Routes may have arrived first; rebuild so the arrive step names the vehicle.
                withVehicle.copy(guidanceByRoute = state.routes.map { buildGuidance(it, withVehicle.destinationLabel) })
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationCallback != null) return
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(permission = LocationPermission.DENIED) }
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(::onLocation)
            }
        }
        locationCallback = callback
        runCatching {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }.onFailure {
            locationCallback = null
            _uiState.update { it.copy(permission = LocationPermission.DENIED) }
        }
    }

    private fun onLocation(location: Location) {
        val fix = LatLng(location.latitude, location.longitude)
        val state = _uiState.value
        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f).roundToInt() else state.speedKmh
        val gpsBearing = if (location.hasBearing() && location.hasSpeed() && location.speed > 1.5f) {
            location.bearing.toDouble()
        } else {
            null
        }

        _uiState.update {
            it.copy(userLocation = fix, speedKmh = speedKmh, userBearing = gpsBearing ?: it.userBearing)
        }

        when (val phase = state.phase) {
            NavigationPhase.Loading -> if (state.routes.isEmpty() && state.destination != null) fetchRoutes(fix)
            is NavigationPhase.Navigating, NavigationPhase.Rerouting -> {
                if (location.accuracy <= 50f) addTripDistance(fix)
                updateProgress(fix, gpsBearing, location.accuracy, phase)
            }
            NavigationPhase.Arrived -> updateArrival(fix)
            else -> Unit
        }
    }

    private fun fetchRoutes(origin: LatLng) {
        val dest = _uiState.value.destination ?: return
        if (routesRequestInFlight) return
        routesRequestInFlight = true
        _uiState.update { it.copy(phase = NavigationPhase.Loading) }
        viewModelScope.launch {
            neshanRepository.getRoute(origin = origin, dest = dest)
                .onSuccess { routes -> applyRoutes(routes, NavigationPhase.Preview) }
                .onFailure {
                    _uiState.update { s -> s.copy(phase = NavigationPhase.Failed("مسیری پیدا نشد. اتصال اینترنت را بررسی کنید.")) }
                }
            routesRequestInFlight = false
        }
    }

    private fun applyRoutes(routes: List<RouteDomainModel>, phase: NavigationPhase) {
        val label = _uiState.value.destinationLabel
        val guidance = routes.map { buildGuidance(it, label) }
        routeLine = routes.firstOrNull()?.let { RouteLine(it.points) }
        hintAlong = null
        offRouteSince = null
        _uiState.update {
            it.copy(
                routes = routes,
                guidanceByRoute = guidance,
                selectedRouteIndex = 0,
                focusedStepIndex = 0,
                progress = null,
                traveledPoints = emptyList(),
                remainingPoints = emptyList(),
                phase = phase,
            )
        }
    }

    fun retry() {
        val location = _uiState.value.userLocation
        if (location != null) fetchRoutes(location) else _uiState.update { it.copy(phase = NavigationPhase.Loading) }
    }

    // --- Preview / steps ---

    fun onRouteSelected(index: Int) {
        val state = _uiState.value
        if (state.phase != NavigationPhase.Preview || index !in state.routes.indices) return
        routeLine = RouteLine(state.routes[index].points)
        _uiState.update { it.copy(selectedRouteIndex = index, focusedStepIndex = 0) }
    }

    fun openSteps() {
        val fromNavigation = _uiState.value.phase is NavigationPhase.Navigating
        _uiState.update {
            it.copy(
                phase = NavigationPhase.Steps(fromNavigation),
                focusedStepIndex = if (fromNavigation) it.progress?.nextStepIndex ?: 0 else 0,
            )
        }
    }

    fun closeSteps() {
        val phase = _uiState.value.phase as? NavigationPhase.Steps ?: return
        _uiState.update {
            it.copy(phase = if (phase.fromNavigation) NavigationPhase.Navigating() else NavigationPhase.Preview)
        }
    }

    fun onStepFocused(index: Int) {
        _uiState.update { it.copy(focusedStepIndex = index) }
    }

    // --- Navigation ---

    fun startNavigation() {
        val state = _uiState.value
        val route = state.selectedRoute ?: return
        if (routeLine?.points !== route.points) routeLine = RouteLine(route.points)
        hintAlong = null
        offRouteSince = null
        tripStartedAt = System.currentTimeMillis()
        tripMeters = 0.0
        lastTripFix = state.userLocation
        _uiState.update { it.copy(phase = NavigationPhase.Navigating(), arrival = null) }
        state.userLocation?.let { updateProgress(it, null, 0f, NavigationPhase.Navigating()) }
    }

    /** End button: back to the route preview, with fresh routes if the driver has moved. */
    fun endNavigation() {
        val state = _uiState.value
        val location = state.userLocation
        val start = state.selectedRoute?.points?.firstOrNull()
        _uiState.update {
            it.copy(progress = null, traveledPoints = emptyList(), remainingPoints = emptyList(), arrival = null)
        }
        if (location != null && (start == null || distanceMeters(location, start) > 50.0)) {
            fetchRoutes(location)
        } else {
            _uiState.update { it.copy(phase = NavigationPhase.Preview) }
        }
    }

    fun showOverview() = setCamera(NavCamera.OVERVIEW)

    fun returnToRoute() = setCamera(NavCamera.FOLLOW)

    /** The driver dragged the map while it was following them. */
    fun onMapGesture() {
        val phase = _uiState.value.phase as? NavigationPhase.Navigating ?: return
        if (phase.camera == NavCamera.FOLLOW) setCamera(NavCamera.FREE)
    }

    private fun setCamera(camera: NavCamera) {
        _uiState.update { state ->
            val phase = state.phase as? NavigationPhase.Navigating ?: return@update state
            state.copy(phase = phase.copy(camera = camera))
        }
    }

    fun setSheetOpen(open: Boolean) {
        _uiState.update { state ->
            val phase = state.phase as? NavigationPhase.Navigating ?: return@update state
            state.copy(phase = phase.copy(sheetOpen = open, stepsOpen = false))
        }
    }

    fun setStepsListOpen(open: Boolean) {
        _uiState.update { state ->
            val phase = state.phase as? NavigationPhase.Navigating ?: return@update state
            state.copy(phase = phase.copy(stepsOpen = open, sheetOpen = false))
        }
    }

    fun toggleVoice() {
        _uiState.update { it.copy(voiceEnabled = !it.voiceEnabled) }
    }

    private fun addTripDistance(fix: LatLng) {
        val last = lastTripFix
        if (last == null) {
            lastTripFix = fix
            return
        }
        val step = distanceMeters(last, fix)
        if (step >= 3.0) {
            tripMeters += step
            lastTripFix = fix
        }
    }

    private fun updateProgress(fix: LatLng, gpsBearing: Double?, accuracy: Float, phase: NavigationPhase) {
        val state = _uiState.value
        val line = routeLine ?: return
        val route = state.selectedRoute ?: return
        val projection = line.project(fix, hintAlong) ?: return
        val progress = computeProgress(line, state.guidance, route.durationSeconds.toDouble(), projection)
        val (traveled, remaining) = line.split(projection)
        val destination = state.destination

        val onRoute = projection.offRoute <= 40.0 || accuracy > 50f
        if (onRoute) hintAlong = projection.along

        _uiState.update {
            it.copy(
                progress = progress,
                traveledPoints = traveled,
                remainingPoints = remaining,
                userBearing = gpsBearing ?: it.userBearing ?: line.bearingAt(projection.along),
            )
        }

        val arrived = progress.remainingMeters <= 25.0 ||
            (destination != null && distanceMeters(fix, destination) <= 20.0)
        if (arrived) {
            arrive(fix)
            return
        }

        val now = System.currentTimeMillis()
        if (onRoute) {
            offRouteSince = null
            if (phase == NavigationPhase.Rerouting && !rerouteInFlight) {
                _uiState.update { it.copy(phase = NavigationPhase.Navigating()) }
            }
            return
        }
        val since = offRouteSince ?: now.also { offRouteSince = it }
        if (now - since >= 5_000) {
            if (phase is NavigationPhase.Navigating) {
                _uiState.update { it.copy(phase = NavigationPhase.Rerouting) }
            }
            requestReroute(fix)
        }
    }

    private fun requestReroute(from: LatLng) {
        val dest = _uiState.value.destination ?: return
        val now = System.currentTimeMillis()
        // At most one request every 10 s, to protect the routing API quota.
        if (rerouteInFlight || now - lastRerouteAt < 10_000) return
        rerouteInFlight = true
        lastRerouteAt = now
        viewModelScope.launch {
            neshanRepository.getRoute(origin = from, dest = dest).onSuccess { routes ->
                if (_uiState.value.phase == NavigationPhase.Rerouting) {
                    applyRoutes(routes.take(1), NavigationPhase.Navigating())
                    _uiState.value.userLocation?.let { updateProgress(it, null, 0f, NavigationPhase.Navigating()) }
                }
            }
            rerouteInFlight = false
        }
    }

    private fun arrive(fix: LatLng) {
        val state = _uiState.value
        val destination = state.destination ?: fix
        _uiState.update {
            it.copy(
                phase = NavigationPhase.Arrived,
                arrival = ArrivalSummary(
                    traveledMeters = tripMeters,
                    tripSeconds = (System.currentTimeMillis() - tripStartedAt) / 1000.0,
                    distanceToVehicle = distanceMeters(fix, destination),
                    sideText = relativeSide(it.userBearing, fix, destination),
                ),
            )
        }
    }

    private fun updateArrival(fix: LatLng) {
        val destination = _uiState.value.destination ?: return
        _uiState.update {
            val arrival = it.arrival ?: return@update it
            it.copy(
                arrival = arrival.copy(
                    distanceToVehicle = distanceMeters(fix, destination),
                    sideText = relativeSide(it.userBearing, fix, destination),
                ),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
}
