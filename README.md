# Number Counter Android app

A simple offline Android counter with an 8-hour session, numeric entries, persistent session state, automatic expiry when the app is reopened/resumed, and a final report on Stop.

## Build an APK with GitHub Actions
1. Create a GitHub repository and upload all files in this folder.
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main`).
4. Open the completed workflow run and download the `NumberCounter-debug-apk` artifact.
5. Extract `app-debug.apk` and install it on Android. Allow installation from your browser/file manager if Android asks.

The app stores the current session locally on the phone. The 8-hour limit is calculated from the saved start time, so it remains accurate if the app is closed or the phone restarts. It automatically marks the session finished the next time the app is opened/resumed after the limit.
