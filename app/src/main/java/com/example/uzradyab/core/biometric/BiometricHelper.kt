package com.example.uzradyab.core.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnabled(): Boolean {
        val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_biometric_enabled", false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_biometric_enabled", enabled).apply()
    }

    fun saveCredentials(phone: String, pass: String) {
        val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_phone", phone)
            .putString("saved_pass", pass)
            .apply()
    }

    fun getSavedPhone(): String? {
        val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
        return prefs.getString("saved_phone", null)
    }

    fun getSavedPassword(): String? {
        val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
        return prefs.getString("saved_pass", null)
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
