# Episode Download Trial — Android

This is a **separate demo Android app**, not a modified DashReels APK.

## Features
- Download All button for three sample videos
- Individual episode downloads
- Download progress, queue, and persistent download IDs
- Offline playback via an installed video player
- Android Download Manager handles background transfers and notifications

## Build
1. Install Android Studio (with Android SDK 35 and JDK 17).
2. Open the `DashReelsTrial` folder as a project.
3. Allow Gradle to sync (internet required for Gradle plugin).
4. Select **Build > Build APK(s)**, or run `gradle assembleDebug` if Gradle is installed.
5. Install `app/build/outputs/apk/debug/app-debug.apk` on your phone.

## Notes
- The demo uses publicly available Google sample video files, not DashReels media.
- It does not access DashReels, scrape video URLs, defeat DRM, or bypass subscriptions.
- Android Download Manager requires network connectivity to retrieve sample videos.
- Large sample videos may use substantial mobile data; Wi-Fi recommended.
- If the same destination filename already exists from a previous attempt, Android Download Manager may fail; delete the old file in Downloads/EpisodeTrial before retrying.
- This project has not been compiled or tested in this environment (Android SDK/Gradle unavailable).
