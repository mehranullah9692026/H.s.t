# HealthCare – Hospital & Medical Services (ہیلتھ کیئر)
Kotlin + Jetpack Compose, Urdu/English (RTL), no permissions, no internet, data stored on device.

## Build (Android Studio, latest stable)
1. File > Open this folder; let Gradle sync (internet needed once to download Gradle 8.9 and libraries).
2. Debug APK: Build > Build APK(s) -> app/build/outputs/apk/debug/
3. Play Store AAB: Build > Generate Signed Bundle/APK > Android App Bundle; create a keystore and KEEP IT SAFE.
## Before Play Store
- Host app/src/main/assets/privacy_policy.html online, add your email, put the URL in Play Console.
- Complete the Data safety form (no data collected) and the Health apps declaration; medicine listings must follow DRAP rules.
- Add screenshots, a 512px icon and a feature graphic.
## Notes
- No fake data: the admin (PIN) adds verified hospitals, doctors, medicines.
- NOT compiled/tested here (no Android SDK in this environment). Open in Android Studio and report any Gradle error.
