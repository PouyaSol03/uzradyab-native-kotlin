package com.example.uzradyab.presentation.device

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.data.remote.api.AuthHelperApi
import com.example.uzradyab.data.remote.dto.AccountChargeDto
import com.example.uzradyab.data.remote.dto.PaymentRequestDto
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RenewCreditViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val authHelperApi: AuthHelperApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceIdStr: String? = savedStateHandle.get<String>("deviceId")
    private val deviceId: Long? = deviceIdStr?.toLongOrNull()

    var device by mutableStateOf<Device?>(null)
        private set

    var accountCharges by mutableStateOf<List<AccountChargeDto>>(emptyList())
        private set

    var selectedPlanId by mutableStateOf<Int?>(null)
        private set

    var selectedGateway by mutableStateOf("zarinpal")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var paymentUrl by mutableStateOf<String?>(null)
        private set

    init {
        fetchData()
    }

    private fun fetchData() {
        if (deviceId == null) {
            errorMessage = "دستگاه مورد نظر یافت نشد"
            isLoading = false
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                device = deviceRepository.getDevice(deviceId)
                accountCharges = authHelperApi.getAccountChargeList()
                if (accountCharges.isNotEmpty()) {
                    selectedPlanId = accountCharges.first().id
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "خطا در دریافت اطلاعات"
            } finally {
                isLoading = false
            }
        }
    }

    fun onPlanSelected(planId: Int) {
        selectedPlanId = planId
    }

    fun onGatewaySelected(gatewayId: String) {
        selectedGateway = gatewayId
    }

    fun getSelectedPlan(): AccountChargeDto? {
        return accountCharges.find { it.id == selectedPlanId }
    }

    fun pay() {
        val plan = getSelectedPlan()
        val currentDevice = device
        if (plan == null || currentDevice == null) return

        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            try {
                val request = PaymentRequestDto(
                    amount = plan.amount.toLongOrNull() ?: 0L,
                    period = plan.period,
                    uniqueId = currentDevice.uniqueId,
                    phone = currentDevice.phone ?: "phone",
                    name = currentDevice.name,
                    id = currentDevice.id,
                    accountChargeId = plan.id
                )
                val response = authHelperApi.pay(request)
                val encodedUrl = java.net.URLEncoder.encode(response.url, "UTF-8")
                paymentUrl = "https://uzkala.com/uzradyab_pay/?payment_url=$encodedUrl"
            } catch (e: Exception) {
                errorMessage = "خطا در برقراری ارتباط با درگاه پرداخت"
            } finally {
                isProcessing = false
            }
        }
    }
    
    fun onPaymentUrlHandled() {
        paymentUrl = null
    }
}
