package com.example.uzradyab.presentation.maintenance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Maintenance
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.MaintenanceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.toImmutable
import javax.inject.Inject

data class MaintenanceUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val devices: ImmutableListWrapper<Device> = emptyImmutableList(),
    val selectedDeviceId: Long? = null,
    val currentOdometerKm: Double = 0.0,
    val maintenances: List<Maintenance> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddEditSheet: Boolean = false,
    val editingMaintenance: Maintenance? = null,
)

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private var initialDeviceId: Long? = savedStateHandle.get<String>("deviceId")?.toLongOrNull()

    init {
        viewModelScope.launch {
            deviceRepository.observeDevices().collect { devices ->
                val currentSelectedId = _uiState.value.selectedDeviceId
                val targetDeviceId = currentSelectedId
                    ?: initialDeviceId
                    ?: devices.firstOrNull()?.id

                val targetDevice = devices.firstOrNull { it.id == targetDeviceId }
                val odometerKm = calculateOdometerKm(targetDevice)

                val shouldLoadMaintenances = currentSelectedId == null && targetDeviceId != null

                _uiState.update { current ->
                    current.copy(
                        devices = devices.toImmutable(),
                        selectedDeviceId = targetDeviceId,
                        currentOdometerKm = odometerKm
                    )
                }

                if (shouldLoadMaintenances && targetDeviceId != null) {
                    loadMaintenances(targetDeviceId)
                }
            }
        }
    }

    fun selectDevice(deviceId: Long) {
        if (_uiState.value.selectedDeviceId == deviceId) return
        viewModelScope.launch {
            val device = _uiState.value.devices.firstOrNull { it.id == deviceId }
            val odometerKm = calculateOdometerKm(device)

            _uiState.update { current ->
                current.copy(
                    selectedDeviceId = deviceId,
                    currentOdometerKm = odometerKm,
                    maintenances = emptyList()
                )
            }
            loadMaintenances(deviceId)
        }
    }

    fun refresh() {
        _uiState.value.selectedDeviceId?.let { loadMaintenances(it) }
    }

    private fun loadMaintenances(deviceId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            maintenanceRepository.getMaintenances(deviceId)
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoading = false, maintenances = items) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "خطا در دریافت لیست سرویس‌ها"
                        )
                    }
                }
        }
    }

    fun markServiceDone(maintenance: Maintenance, customKm: Double? = null) {
        val currentKm = customKm ?: _uiState.value.currentOdometerKm
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            maintenanceRepository.resetMaintenance(maintenance.id, currentKm, maintenance)
                .onSuccess { updated ->
                    _uiState.update { current ->
                        val updatedList = current.maintenances.map {
                            if (it.id == updated.id) updated else it
                        }
                        current.copy(
                            isSubmitting = false,
                            maintenances = updatedList,
                            successMessage = "سرویس ${maintenance.name} با موفقیت ثبت شد"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "خطا در ثبت انجام سرویس"
                        )
                    }
                }
        }
    }

    fun saveService(name: String, periodKm: Double, startKm: Double) {
        val deviceId = _uiState.value.selectedDeviceId ?: return
        val editing = _uiState.value.editingMaintenance

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (editing != null) {
                maintenanceRepository.updateMaintenance(editing.id, name, periodKm, startKm)
            } else {
                maintenanceRepository.createMaintenance(deviceId, name, periodKm, startKm)
            }

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isSubmitting = false,
                        showAddEditSheet = false,
                        editingMaintenance = null,
                        successMessage = if (editing != null) "سرویس با موفقیت ویرایش شد" else "سرویس با موفقیت ایجاد شد"
                    )
                }
                loadMaintenances(deviceId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.localizedMessage ?: "خطا در ذخیره سرویس"
                    )
                }
            }
        }
    }

    fun deleteService(id: Long) {
        val deviceId = _uiState.value.selectedDeviceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            maintenanceRepository.deleteMaintenance(id)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            maintenances = current.maintenances.filterNot { it.id == id },
                            successMessage = "سرویس با موفقیت حذف شد"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "خطا در حذف سرویس"
                        )
                    }
                }
        }
    }

    fun openAddSheet() {
        _uiState.update { it.copy(showAddEditSheet = true, editingMaintenance = null) }
    }

    fun openEditSheet(maintenance: Maintenance) {
        _uiState.update { it.copy(showAddEditSheet = true, editingMaintenance = maintenance) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, editingMaintenance = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun calculateOdometerKm(device: Device?): Double {
        if (device == null) return 0.0

        // Priority 1: From Device Info (تنظیمات دستگاه: کیلومتر فعلی دستگاه)
        if (device.currentKilometers.isNotBlank()) {
            val km = device.currentKilometers.toDoubleOrNull()
            if (km != null && km >= 0) {
                return km
            }
        }

        // Priority 2: Direct lookup in device.attributesJson in case
        if (device.attributesJson.isNotBlank()) {
            try {
                val regex = "\"currentKilometers\"\\s*:\\s*\"?(\\d+(?:\\.\\d+)?)\"?".toRegex()
                val match = regex.find(device.attributesJson)
                if (match != null) {
                    val km = match.groupValues[1].toDoubleOrNull()
                    if (km != null && km >= 0) {
                        return km
                    }
                }
            } catch (_: Exception) {}
        }

        return 0.0
    }
}
