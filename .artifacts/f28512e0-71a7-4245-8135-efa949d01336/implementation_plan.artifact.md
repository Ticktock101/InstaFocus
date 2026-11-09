# InstaFocus App Implementation

This app will use an Android Accessibility Service to monitor Instagram (`com.instagram.android`) and prevent you from accessing the Reels and Explore pages. Because you only intend to use it on your personal Samsung S25, we don't need to worry about Google Play Store restrictions for Accessibility Services.

## User Review Required

> [!IMPORTANT]
> Since I cannot directly read the attached PDF, I have proposed a standard approach for the "bypass" feature: a simple ON/OFF switch in the app. If your PDF specified a different bypass method (e.g., a timer, a password, or a math problem), please let me know so I can adjust the plan!

## Proposed Changes

We will build the app using Kotlin and Jetpack Compose for the UI.

### 1. Accessibility Service Configuration
We need to declare our service to the Android system and configure it to only listen to Instagram.

#### [NEW] `app/src/main/res/xml/accessibility_service_config.xml`
- Configures the service to listen to window state changes and content changes specifically for the `com.instagram.android` package.

#### [MODIFY] `app/src/main/AndroidManifest.xml`
- Register the Accessibility Service and request the `BIND_ACCESSIBILITY_SERVICE` permission.

### 2. Core Logic (The Blocker)
#### [NEW] `app/src/main/java/com/example/instafocus/InstagramBlockerService.kt`
- An `AccessibilityService` that analyzes the screen when you are in Instagram.
- It will look for the "Reels" and "Explore/Search" tabs (by inspecting the UI nodes' text and content descriptions).
- If you enter these tabs and the bypass is *off*, it will automatically execute a system "Back" press to kick you out of the tab, or redirect you to the Home tab.

#### [NEW] `app/src/main/java/com/example/instafocus/PreferencesManager.kt`
- A lightweight class using `SharedPreferences` to save whether the bypass is currently ON or OFF, so the Accessibility Service knows what to do.

### 3. User Interface
#### [NEW] `app/src/main/java/com/example/instafocus/MainActivity.kt`
- A clean Jetpack Compose screen with two main elements:
  1. A button to open Android Settings if the Accessibility Service is not yet enabled.
  2. A large switch/button to toggle the "Bypass Block" feature on or off.

## Verification Plan

### Manual Verification
1. I will provide the code. You will build and install the app on your Samsung S25.
2. Open InstaFocus and grant it Accessibility permissions.
3. Open Instagram. Try to navigate to Reels or the Explore page. The app should immediately back you out of those pages.
4. Go back to InstaFocus, turn on the "Bypass", and verify you can now access Reels and Explore.
