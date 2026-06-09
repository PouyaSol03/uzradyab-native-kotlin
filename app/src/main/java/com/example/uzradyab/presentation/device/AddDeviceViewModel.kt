package com.example.uzradyab.presentation.device

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDeviceViewModel @Inject constructor(
    private val repository: DeviceRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val deviceId: Long? = savedStateHandle.get<String>("deviceId")?.toLongOrNull()

    var isReadOnly by mutableStateOf(savedStateHandle.get<Boolean>("isReadOnly") ?: false)
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var creditText by mutableStateOf("")
        private set

    var endCreditText by mutableStateOf("")
        private set

    var name by mutableStateOf("")
        private set

    var uniqueId by mutableStateOf("")
        private set

    var phone by mutableStateOf("")
        private set

    var currentKilometers by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    init {
        deviceId?.let { id ->
            isEditMode = true
            viewModelScope.launch {
                isLoading = true
                val device = repository.getDevice(id)
                device?.let {
                    name = it.name
                    uniqueId = it.uniqueId
                    phone = it.phone.orEmpty()
                    
                    val currentKm = try {
                        val obj = com.google.gson.JsonParser.parseString(it.attributesJson).asJsonObject
                        if (obj.has("currentKilometers")) {
                            obj.get("currentKilometers").asString
                        } else ""
                    } catch (e: Exception) {
                        ""
                    }
                    currentKilometers = currentKm
                    
                    it.expirationTime?.let { expTime ->
                        val persianDate = formatGregorianToJalali(expTime)
                        if (persianDate.isNotEmpty()) {
                            creditText = "اعتبار 1 ساله اکسیر"
                            endCreditText = "پایان: $persianDate"
                        }
                    }
                }
                isLoading = false
            }
        }
    }

    fun onNameChange(newValue: String) {
        name = newValue
    }

    fun onUniqueIdChange(newValue: String) {
        // IMEI shouldn't be edited in edit mode
        if (!isEditMode) {
            uniqueId = newValue
        }
    }

    fun onPhoneChange(newValue: String) {
        phone = newValue
    }

    fun onCurrentKilometersChange(newValue: String) {
        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
            currentKilometers = newValue
        }
    }

    val isFormValid: Boolean
        get() = name.isNotBlank() && uniqueId.isNotBlank()

    fun saveDevice() {
        if (!isFormValid) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val mileage = currentKilometers.toDoubleOrNull()
            
            val result = if (isEditMode && deviceId != null) {
                repository.updateDevice(
                    id = deviceId,
                    name = name,
                    uniqueId = uniqueId,
                    phone = phone,
                    currentKilometers = mileage
                )
            } else {
                repository.addDevice(
                    name = name,
                    uniqueId = uniqueId,
                    phone = phone,
                    currentKilometers = mileage
                )
            }

            result.onSuccess {
                isSuccess = true
            }.onFailure { exception ->
                errorMessage = exception.localizedMessage ?: "خطایی در ثبت دستگاه رخ داد."
            }
            
            isLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            signedOut = true
        }
    }

    var signedOut by mutableStateOf(false)
        private set

    // Jalali Date Helper
    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 335)
        val gy2 = if (gm > 2) gy + 1 else gy
        var gDays = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDaysInMonth[gm - 1] + gd
        var jy = -1595 + 33 * (gDays / 12053)
        gDays %= 12053
        jy += 4 * (gDays / 1461)
        gDays %= 1461
        if (gDays > 365) {
            jy += ((gDays - 1) / 365)
            gDays = (gDays - 1) % 365
        }
        val jm = if (gDays < 186) 1 + (gDays / 31) else 7 + ((gDays - 186) / 30)
        val jd = 1 + (if (gDays < 186) gDays % 31 else (gDays - 186) % 30)
        return intArrayOf(jy, jm, jd)
    }

    private val jalaliMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private fun formatGregorianToJalali(dateStr: String): String {
        return try {
            val parts = dateStr.take(10).split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull()
                val month = parts[1].toIntOrNull()
                val day = parts[2].toIntOrNull()
                if (year != null && month != null && day != null) {
                    val jalali = gregorianToJalali(year, month, day)
                    val monthName = jalaliMonths.getOrNull(jalali[1] - 1) ?: ""
                    "${jalali[2]} $monthName ${jalali[0]}"
                } else ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }
}
