package com.example.instafocus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class InstagramBlockerService : AccessibilityService() {

    private val TAG = "InstaFocusBlocker"
    private var isDebugMode = true // Set to true to log view hierarchy

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != "com.instagram.android") return

        val rootNode = rootInActiveWindow ?: return

        if (isDebugMode) {
            // Uncomment to log the full view tree for debugging Instagram's UI
            // logViewTree(rootNode, 0)
        }

        if (BypassManager.getInstance(this).checkIfBypassActive()) {
            return // Bypass is active, allow everything
        }

        // Check if we are on a screen with the bottom navigation bar
        // Usually, when viewing a Reel from a DM, the bottom nav bar is NOT present.
        val reelsTab = findNodeByContentDescSubstring(rootNode, "Reels")
        val exploreTab = findNodeByContentDescSubstring(rootNode, "Search and explore")

        // We only care if the bottom nav tabs are present on screen.
        if (reelsTab == null && exploreTab == null) {
            return // Not on main tabs, probably in DM or search results or watching a reel from DM
        }

        val isReelsSelected = reelsTab?.isSelected == true || reelsTab?.contentDescription?.toString()?.contains("Selected", ignoreCase = true) == true
        val isExploreSelected = exploreTab?.isSelected == true || exploreTab?.contentDescription?.toString()?.contains("Selected", ignoreCase = true) == true

        if (isReelsSelected) {
            Log.d(TAG, "Reels tab is selected. Blocking.")
            blockAndGoHome(rootNode)
        } else if (isExploreSelected) {
            // Check if search is active. Usually, there's an EditText or a "Search" node that is focused.
            val searchBox = findNodeByClassName(rootNode, "android.widget.EditText")
            val isSearchFocused = searchBox?.isFocused == true || searchBox?.text?.isNotEmpty() == true

            if (!isSearchFocused) {
                Log.d(TAG, "Explore tab is selected and search is not active. Blocking.")
                blockAndGoHome(rootNode)
            }
        }
    }

    private fun blockAndGoHome(rootNode: AccessibilityNodeInfo) {
        // 1. Try to click Home tab
        val homeTab = findNodeByContentDescSubstring(rootNode, "Home")
        if (homeTab != null && homeTab.isClickable) {
            homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            // Fallback: use global back action (might just close the app if on main screen, which is fine)
            // Or try to find it and click its parent
            var current = homeTab
            while (current != null && !current.isClickable) {
                current = current.parent
            }
            current?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // 2. Launch Block Screen
        val intent = Intent(this, BlockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Required by AccessibilityService
    }

    private fun findNodeByContentDescSubstring(root: AccessibilityNodeInfo, substring: String): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val contentDesc = node.contentDescription?.toString()
            if (contentDesc != null && contentDesc.contains(substring, ignoreCase = true)) {
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun findNodeByClassName(root: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.className?.toString() == className) {
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun logViewTree(node: AccessibilityNodeInfo?, depth: Int) {
        if (node == null) return
        val indent = " ".repeat(depth * 2)
        Log.d(TAG, "$indent Node: class=${node.className}, desc=${node.contentDescription}, text=${node.text}, selected=${node.isSelected}, focused=${node.isFocused}, clickable=${node.isClickable}")
        for (i in 0 until node.childCount) {
            logViewTree(node.getChild(i), depth + 1)
        }
    }
}
