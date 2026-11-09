package com.example.instafocus

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BypassManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isBypassActive = MutableStateFlow(false)
    val isBypassActive: StateFlow<Boolean> = _isBypassActive.asStateFlow()

    init {
        _isBypassActive.value = checkIfBypassActive()
    }

    fun activateBypass() {
        val expiryTime = System.currentTimeMillis() + BYPASS_DURATION_MS
        prefs.edit().putLong(KEY_BYPASS_EXPIRY, expiryTime).apply()
        _isBypassActive.value = true
    }

    fun endBypass() {
        prefs.edit().remove(KEY_BYPASS_EXPIRY).apply()
        _isBypassActive.value = false
    }

    fun checkIfBypassActive(): Boolean {
        val expiryTime = prefs.getLong(KEY_BYPASS_EXPIRY, 0L)
        val isActive = System.currentTimeMillis() < expiryTime
        if (!isActive && expiryTime != 0L) {
            // Clean up expired bypass
            prefs.edit().remove(KEY_BYPASS_EXPIRY).apply()
        }
        
        return isActive
    }

    fun getExpiryTime(): Long {
        return prefs.getLong(KEY_BYPASS_EXPIRY, 0L)
    }

    companion object {
        private const val PREFS_NAME = "instafocus_prefs"
        private const val KEY_BYPASS_EXPIRY = "bypass_expiry"
        private const val BYPASS_DURATION_MS = 10 * 60 * 1000L // 10 minutes

        @Volatile
        private var INSTANCE: BypassManager? = null

        fun getInstance(context: Context): BypassManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BypassManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
