package com.example.uzradyab.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureCredentialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val encryptedPrefs = try {
            createEncryptedPreferences()
        } catch (e: Exception) {
            context.deleteSharedPreferences(PREFS_NAME)
            createEncryptedPreferences()
        }

        // Migration from legacy unencrypted prefs if they exist
        try {
            val oldPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            if (oldPrefs.contains(KEY_SAVED_PHONE) || oldPrefs.contains(KEY_SAVED_PASS)) {
                val oldPhone = oldPrefs.getString(KEY_SAVED_PHONE, null)
                val oldPass = oldPrefs.getString(KEY_SAVED_PASS, null)
                if (!oldPhone.isNullOrBlank() && !oldPass.isNullOrBlank()) {
                    encryptedPrefs.edit()
                        .putString(KEY_SAVED_PHONE, oldPhone)
                        .putString(KEY_SAVED_PASS, oldPass)
                        .putBoolean(KEY_REMEMBER_ME, true)
                        .apply()
                }
                oldPrefs.edit().clear().apply()
            }
        } catch (_: Exception) {
            // Ignore legacy migration failure
        }

        encryptedPrefs
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Checks whether "Remember Me" is active.
     * Defaults to true if credentials already exist or for first-time login.
     */
    fun isRememberMeEnabled(): Boolean {
        return if (prefs.contains(KEY_REMEMBER_ME)) {
            prefs.getBoolean(KEY_REMEMBER_ME, true)
        } else {
            // Default to true so users don't get locked out
            true
        }
    }

    /**
     * Toggles "Remember Me" preference.
     * When disabled, saved credentials are purged.
     */
    fun setRememberMeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
        if (!enabled) {
            clearCredentials()
        }
    }

    /**
     * Securely encrypts and stores the username (phone) and password using Android Keystore AES-256 GCM.
     */
    fun saveCredentials(phone: String, pass: String, rememberMe: Boolean = true) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_SAVED_PHONE, phone)
            .putString(KEY_SAVED_PASS, pass)
            .apply()
    }

    /**
     * Retrieves the securely saved phone number, if any.
     */
    fun getSavedPhone(): String? {
        return prefs.getString(KEY_SAVED_PHONE, null)
    }

    /**
     * Retrieves the securely saved password, if any.
     */
    fun getSavedPassword(): String? {
        return prefs.getString(KEY_SAVED_PASS, null)
    }

    /**
     * Clears all saved credentials from encrypted storage.
     */
    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_SAVED_PHONE)
            .remove(KEY_SAVED_PASS)
            .putBoolean(KEY_REMEMBER_ME, false)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "biometric_prefs_secure"
        private const val LEGACY_PREFS_NAME = "biometric_prefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_PHONE = "saved_phone"
        private const val KEY_SAVED_PASS = "saved_pass"
    }
}
