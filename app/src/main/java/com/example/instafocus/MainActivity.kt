package com.example.instafocus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var settingsButton: Button
    private lateinit var endBypassButton: Button
    private lateinit var bypassStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        statusTextView = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        settingsButton = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        bypassStatusText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 64, 0, 32)
        }

        endBypassButton = Button(this).apply {
            text = "End Bypass Early"
            setOnClickListener {
                BypassManager.getInstance(this@MainActivity).endBypass()
                updateUI()
            }
        }

        rootLayout.addView(statusTextView)
        rootLayout.addView(settingsButton)
        rootLayout.addView(bypassStatusText)
        rootLayout.addView(endBypassButton)

        setContentView(rootLayout)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val isServiceEnabled = isAccessibilityServiceEnabled(this, InstagramBlockerService::class.java)
        if (isServiceEnabled) {
            statusTextView.text = "InstaFocus Blocker is ENABLED"
            statusTextView.setTextColor(android.graphics.Color.parseColor("#006400")) // Dark Green
        } else {
            statusTextView.text = "InstaFocus Blocker is DISABLED\nPlease enable it in Settings."
            statusTextView.setTextColor(android.graphics.Color.RED)
        }

        val bypassManager = BypassManager.getInstance(this)
        val isBypassActive = bypassManager.checkIfBypassActive()

        if (isBypassActive) {
            val expiryTime = bypassManager.getExpiryTime()
            val remainingMs = expiryTime - System.currentTimeMillis()
            val remainingMinutes = remainingMs / 1000 / 60
            bypassStatusText.text = "Bypass is ACTIVE.\nExpires in ~${remainingMinutes} minutes."
            endBypassButton.visibility = android.view.View.VISIBLE
        } else {
            bypassStatusText.text = "Bypass is NOT active."
            endBypassButton.visibility = android.view.View.GONE
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, accessibilityService: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityService)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }
}
