package com.example.uzradyab.presentation.command

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.CommandRequestDto
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommandCenterViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val authRepository: AuthRepository,
    private val traccarApi: TraccarApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val deviceId: Long? = savedStateHandle.get<String>("deviceId")?.toLongOrNull()

    var phoneNumber by mutableStateOf("")
        private set

    var userEmail by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSending by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    init {
        deviceId?.let { id ->
            viewModelScope.launch {
                isLoading = true
                val device = deviceRepository.getDevice(id)
                device?.phone?.let {
                    phoneNumber = it
                }
                isLoading = false
            }
        }
        
        viewModelScope.launch {
            authRepository.currentSession.collectLatest { session ->
                session?.email?.let {
                    userEmail = it
                }
            }
        }
    }

    fun sendCommandInternet(commandType: String, commandString: String) {
        if (deviceId == null) return
        viewModelScope.launch {
            isSending = true
            errorMessage = null
            isSuccess = false

            try {
                // Post to /api/commands/send
                val requestBody = CommandRequestDto(
                    type = "custom",
                    deviceId = deviceId,
                    attributes = mapOf("data" to commandString)
                )
                
                traccarApi.sendCommand(requestBody)
                isSuccess = true
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "خطایی در ارسال دستور رخ داد"
            } finally {
                isSending = false
            }
        }
    }
}
