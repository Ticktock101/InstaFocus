package com.example.instafocus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class BlockScreenActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var isWaiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root layout
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 150) // Padding at bottom
        }

        val button = Button(this).apply {
            text = "Bypass"
        }

        button.setOnClickListener {
            if (!isWaiting && button.text == "Bypass") {
                // Start wait
                isWaiting = true
                button.isEnabled = false // Disable it to prevent multiple clicks
                button.alpha = 0f // Make it invisible (subtle)

                // Wait 15 seconds
                handler.postDelayed({
                    isWaiting = false
                    button.isEnabled = true
                    button.alpha = 1f // Show again
                    button.text = "Unlock for 10 Minutes"
                }, 15000L)
            } else if (!isWaiting && button.text == "Unlock for 10 Minutes") {
                BypassManager.getInstance(this@BlockScreenActivity).activateBypass()
                finish() // Close Activity, return to Instagram
            }
        }

        rootLayout.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        setContentView(rootLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Prevent memory leaks
    }
}
