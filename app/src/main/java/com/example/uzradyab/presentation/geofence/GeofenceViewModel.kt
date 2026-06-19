package com.example.uzradyab.presentation.geofence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Geofence
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.GeofenceRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DrawMode {
    CIRCLE, POLYGON, LINESTRING
}

data class GeofenceState(
    val isLoading: Boolean = false,
    val geofences: List<Geofence> = emptyList(),
    val error: String? = null,
    val deviceId: Long? = null,
    val devicePosition: Position? = null,
    val addingMode: Boolean = false,
    val drawMode: DrawMode = DrawMode.CIRCLE,
    val newGeofenceName: String = "",
    val activeDrawingPoints: List<Pair<Double, Double>> = emptyList(),
    val newGeofenceRadius: Double = 500.0,
    val selectedGeofenceId: Long? = null,
    val mapStyle: String = "osm"
)

@HiltViewModel
class GeofenceViewModel @Inject constructor(
    private val geofenceRepository: GeofenceRepository,
    private val positionRepository: PositionRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GeofenceState())
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
                _state.update { it.copy(geofences = geofences) }
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
                activeDrawingPoints = initialPoints,
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
        _state.update { it.copy(drawMode = mode, activeDrawingPoints = emptyList()) }
    }

    fun addDrawingPoint(lat: Double, lon: Double) {
        _state.update {
            if (!it.addingMode) return@update it
            val newPoints = if (it.drawMode == DrawMode.CIRCLE) {
                listOf(Pair(lat, lon))
            } else {
                it.activeDrawingPoints + Pair(lat, lon)
            }
            it.copy(activeDrawingPoints = newPoints)
        }
    }

    fun undoLastDrawingPoint() {
        _state.update {
            val newPoints = if (it.activeDrawingPoints.isNotEmpty()) {
                it.activeDrawingPoints.dropLast(1)
            } else {
                it.activeDrawingPoints
            }
            it.copy(activeDrawingPoints = newPoints)
        }
    }

    fun clearDrawingPoints() {
        _state.update { it.copy(activeDrawingPoints = emptyList()) }
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
                    _state.update { it.copy(isLoading = false, geofences = geofences, selectedGeofenceId = null) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = deleteResult.exceptionOrNull()?.message) }
            }
        }
    }

    fun saveNewGeofence() {
        val st = _state.value
        val points = st.activeDrawingPoints

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
                    _state.update { it.copy(isLoading = false, addingMode = false, geofences = geofences) }
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
