package com.example.uzradyab.presentation.geofence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Geofence
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.GeofenceRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.toImmutable

enum class DrawMode {
    CIRCLE, POLYGON, LINESTRING
}

data class GeofenceState(
    val isLoading: Boolean = false,
    val geofences: ImmutableListWrapper<Geofence> = emptyImmutableList(),
    val error: String? = null,
    val deviceId: Long? = null,
    val devicePosition: Position? = null,
    val addingMode: Boolean = false,
    val drawMode: DrawMode = DrawMode.CIRCLE,
    val newGeofenceName: String = "",
    val activeDrawingPoints: ImmutableListWrapper<Pair<Double, Double>> = emptyImmutableList(),
    val newGeofenceRadius: Double = 500.0,
    val selectedGeofenceId: Long? = null,
    val mapStyle: String = "osm",
    val connectionsGeofence: Geofence? = null,
    val connectionsLoading: Boolean = false,
    val connectionsDevices: ImmutableListWrapper<Device> = emptyImmutableList(),
    val linkedDeviceIds: Set<Long> = emptySet(),
    val pendingDeviceIds: Set<Long> = emptySet(),
    val connectionsSearchQuery: String = "",
    val connectionsError: String? = null
)

@HiltViewModel
class GeofenceViewModel @Inject constructor(
    private val geofenceRepository: GeofenceRepository,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GeofenceState(mapStyle = mapSettingsRepository.getMapStyleSync()))
    val state = _state.asStateFlow()

    init {
        val deviceIdStr = savedStateHandle.get<String>("deviceId")
        val deviceId = deviceIdStr?.toLongOrNull()

        if (deviceId != null) {
            _state.update { it.copy(deviceId = deviceId) }
            // Even if we don't strictly need deviceId for geofences,
            // we pass it here to get the initial map position.
            loadData(deviceId)
        } else {
            _state.update { it.copy(error = "Device ID missing") }
            // You can still load geofences even if the device ID is missing
            loadData(null)
        }

        viewModelScope.launch {
            mapSettingsRepository.observeMapStyle().collect { style ->
                _state.update { it.copy(mapStyle = style) }
            }
        }
    }

    private fun loadData(deviceId: Long?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // 1. Load ALL geofences for the user account (No deviceId required)
            val result = geofenceRepository.getGeofences()
            result.onSuccess { geofences ->
                val initialSelectedId = if (geofences.size == 1) geofences.first().id else null
                _state.update { it.copy(geofences = geofences.toImmutable(), selectedGeofenceId = initialSelectedId) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }

            // 2. Load device position for map center (If a deviceId exists)
            if (deviceId != null) {
                val devicePos = positionRepository.getLatestPosition(deviceId)
                if (devicePos != null) {
                    _state.update { it.copy(devicePosition = devicePos) }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun toggleAddingMode() {
        _state.update {
            val initialPoints = it.devicePosition?.let { pos -> listOf(Pair(pos.latitude, pos.longitude)) } ?: emptyList()
            it.copy(
                addingMode = !it.addingMode,
                drawMode = DrawMode.CIRCLE,
                newGeofenceName = "",
                activeDrawingPoints = initialPoints.toImmutable(),
                newGeofenceRadius = 500.0,
                selectedGeofenceId = null
            )
        }
    }

    fun updateNewGeofenceName(name: String) {
        _state.update { it.copy(newGeofenceName = name) }
    }

    fun updateNewGeofenceRadius(radius: Double) {
        _state.update { it.copy(newGeofenceRadius = radius) }
    }

    fun setDrawMode(mode: DrawMode) {
        _state.update { it.copy(drawMode = mode, activeDrawingPoints = emptyImmutableList()) }
    }

    fun addDrawingPoint(lat: Double, lon: Double) {
        _state.update { state ->
            if (!state.addingMode) return@update state
            val newPoints = if (state.drawMode == DrawMode.CIRCLE) {
                listOf(Pair(lat, lon))
            } else {
                val current = state.activeDrawingPoints.items.toMutableList()
                current.add(lat to lon)
                current
            }
            state.copy(activeDrawingPoints = newPoints.toImmutable())
        }
    }

    fun undoLastDrawingPoint() {
        _state.update { state ->
            val current = state.activeDrawingPoints.items.toMutableList()
            if (current.isNotEmpty()) {
                current.removeAt(current.lastIndex)
            }
            state.copy(activeDrawingPoints = current.toImmutable())
        }
    }

    fun clearDrawingPoints() {
        _state.update { it.copy(activeDrawingPoints = emptyImmutableList()) }
    }

    fun selectGeofence(id: Long?) {
        _state.update { it.copy(selectedGeofenceId = id, addingMode = false) }
    }

    fun deleteGeofence(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val deleteResult = geofenceRepository.deleteGeofence(id)
            if (deleteResult.isSuccess) {
                // Reload the account's full list of geofences (No deviceId needed)
                val listResult = geofenceRepository.getGeofences()
                listResult.onSuccess { geofences ->
                    _state.update { it.copy(isLoading = false, geofences = geofences.toImmutable(), selectedGeofenceId = null) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = deleteResult.exceptionOrNull()?.message) }
            }
        }
    }

    fun saveNewGeofence() {
        val st = _state.value
        val points = st.activeDrawingPoints.items

        if (points.isEmpty()) return
        if (st.drawMode == DrawMode.POLYGON && points.size < 3) return
        if (st.drawMode == DrawMode.LINESTRING && points.size < 2) return

        val name = st.newGeofenceName.ifBlank { "محدوده جدید" }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val areaString = when (st.drawMode) {
                DrawMode.CIRCLE -> Geofence.buildCircleArea(points[0].first, points[0].second, st.newGeofenceRadius)
                DrawMode.POLYGON -> Geofence.buildPolygonArea(points)
                DrawMode.LINESTRING -> Geofence.buildLineStringArea(points)
            }

            // Create the geofence on the server
            val createResult = geofenceRepository.createGeofence(name, areaString)

            createResult.onSuccess {
                // Skip the device linking! Just refresh the account geofences.
                val listResult = geofenceRepository.getGeofences()
                listResult.onSuccess { geofences ->
                    _state.update { it.copy(isLoading = false, addingMode = false, geofences = geofences.toImmutable()) }
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun openConnections(geofence: Geofence) {
        isFetchingMoreConnections = false
        _state.update {
            it.copy(
                connectionsGeofence = geofence,
                connectionsLoading = true,
                connectionsError = null,
                connectionsSearchQuery = "",
                connectionsDevices = emptyImmutableList(),
                linkedDeviceIds = emptySet(),
                pendingDeviceIds = emptySet()
            )
        }
        viewModelScope.launch {
            try {
                // Fresh fetch from server for currently logged in user
                deviceRepository.refreshDevices(limit = 30, offset = 0)
                val devices = try {
                    deviceRepository.observeDevices().first()
                } catch (e: Exception) {
                    emptyList()
                }
                _state.update { it.copy(connectionsDevices = devices.toImmutable()) }

                if (devices.isEmpty()) {
                    _state.update { it.copy(connectionsLoading = false) }
                    return@launch
                }

                // Accurate per-device geofence lookup in parallel
                val linkedIds = coroutineScope {
                    devices.map { device ->
                        async {
                            val res = geofenceRepository.getDeviceGeofences(device.id)
                            val isLinked = res.getOrNull()?.any { it.id == geofence.id } == true
                            if (isLinked) device.id else null
                        }
                    }.awaitAll().filterNotNull().toSet()
                }
                _state.update {
                    it.copy(
                        connectionsLoading = false,
                        linkedDeviceIds = linkedIds
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        connectionsLoading = false,
                        connectionsError = e.message ?: "خطا در دریافت وضعیت اتصالات دستگاه‌ها"
                    )
                }
            }
        }
    }

    fun refreshConnections() {
        val geofence = _state.value.connectionsGeofence ?: return
        openConnections(geofence)
    }

    private var isFetchingMoreConnections = false

    fun loadMoreConnectionsDevices() {
        val geofence = _state.value.connectionsGeofence ?: return
        if (isFetchingMoreConnections) return
        isFetchingMoreConnections = true
        _state.update { it.copy(connectionsLoading = true) }
        viewModelScope.launch {
            try {
                val previousIds = _state.value.connectionsDevices.items.map { it.id }.toSet()
                val loadedCount = deviceRepository.loadMoreDevices(limit = 30).getOrDefault(0)
                if (loadedCount > 0) {
                    val allDevices = try { deviceRepository.observeDevices().first() } catch (e: Exception) { emptyList() }
                    // Immediately show the new devices in UI
                    _state.update { it.copy(connectionsDevices = allDevices.toImmutable()) }

                    // Only query permissions for newly added devices
                    val newlyAdded = allDevices.filter { it.id !in previousIds }
                    if (newlyAdded.isNotEmpty()) {
                        val newLinkedIds = coroutineScope {
                            newlyAdded.map { device ->
                                async {
                                    val res = geofenceRepository.getDeviceGeofences(device.id)
                                    val isLinked = res.getOrNull()?.any { it.id == geofence.id } == true
                                    if (isLinked) device.id else null
                                }
                            }.awaitAll().filterNotNull().toSet()
                        }
                        _state.update { it.copy(linkedDeviceIds = it.linkedDeviceIds + newLinkedIds) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GeofenceViewModel", "Failed to load more devices: ${e.message}", e)
            } finally {
                _state.update { it.copy(connectionsLoading = false) }
                isFetchingMoreConnections = false
            }
        }
    }

    fun closeConnections() {
        isFetchingMoreConnections = false
        _state.update {
            it.copy(
                connectionsGeofence = null,
                connectionsLoading = false,
                connectionsError = null,
                connectionsSearchQuery = "",
                pendingDeviceIds = emptySet()
            )
        }
    }

    fun updateConnectionsSearchQuery(query: String) {
        _state.update { it.copy(connectionsSearchQuery = query) }
    }

    fun toggleDeviceConnection(geofenceId: Long, deviceId: Long, shouldLink: Boolean) {
        val currentState = _state.value
        if (currentState.pendingDeviceIds.contains(deviceId)) return

        val currentLinked = currentState.linkedDeviceIds
        val newLinked = if (shouldLink) currentLinked + deviceId else currentLinked - deviceId

        _state.update {
            it.copy(
                linkedDeviceIds = newLinked,
                pendingDeviceIds = it.pendingDeviceIds + deviceId,
                connectionsError = null
            )
        }

        viewModelScope.launch {
            val result = if (shouldLink) {
                geofenceRepository.linkDeviceToGeofence(deviceId, geofenceId)
            } else {
                geofenceRepository.unlinkDeviceFromGeofence(deviceId, geofenceId)
            }

            _state.update { state ->
                val updatedPending = state.pendingDeviceIds - deviceId
                if (result.isSuccess) {
                    state.copy(pendingDeviceIds = updatedPending)
                } else {
                    state.copy(
                        linkedDeviceIds = currentLinked,
                        pendingDeviceIds = updatedPending,
                        connectionsError = result.exceptionOrNull()?.message ?: "خطا در تغییر اتصال دستگاه"
                    )
                }
            }
        }
    }

    fun toggleAllDeviceConnections(geofenceId: Long, linkAll: Boolean) {
        val currentState = _state.value
        val allDevices = currentState.connectionsDevices.items
        if (allDevices.isEmpty()) return

        val targetDevices = if (linkAll) {
            allDevices.filter { it.id !in currentState.linkedDeviceIds }
        } else {
            allDevices.filter { it.id in currentState.linkedDeviceIds }
        }
        if (targetDevices.isEmpty()) return

        val targetIds = targetDevices.map { it.id }.toSet()
        val currentLinked = currentState.linkedDeviceIds
        val newLinked = if (linkAll) currentLinked + targetIds else currentLinked - targetIds

        _state.update {
            it.copy(
                linkedDeviceIds = newLinked,
                pendingDeviceIds = it.pendingDeviceIds + targetIds,
                connectionsError = null
            )
        }

        viewModelScope.launch {
            coroutineScope {
                targetDevices.map { device ->
                    async {
                        val res = if (linkAll) {
                            geofenceRepository.linkDeviceToGeofence(device.id, geofenceId)
                        } else {
                            geofenceRepository.unlinkDeviceFromGeofence(device.id, geofenceId)
                        }
                        device.id to res.isSuccess
                    }
                }.awaitAll()
            }

            _state.update { it.copy(pendingDeviceIds = it.pendingDeviceIds - targetIds) }
        }
    }
}
