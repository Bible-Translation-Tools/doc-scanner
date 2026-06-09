This is a Kotlin Multiplatform project targeting Android and iOS.

* `/shared` is the Compose Multiplatform module shared across platforms.
  - `commonMain` — code common to all targets (UI, view models, repositories, networking).
  - `androidMain` / `iosMain` — platform-specific `actual` implementations
    (document scanner, PDF rendering, file share/open, database driver, preferences, directories).

* `/androidApp` is the Android application entry point (`com.android.application`).

* `/iosApp` is the Xcode project for the iOS application. It hosts the shared Compose UI
  through `MainViewControllerKt.MainViewController()`.

## Building

### Android
```
./gradlew :androidApp:assembleDebug
```

### iOS
Requires **full Xcode** (not just Command Line Tools).

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Set your signing **Team** (Signing & Capabilities), or fill `TEAM_ID` in
   `iosApp/Configuration/Config.xcconfig`.
3. Select an iOS Simulator (Apple Silicon) or a device and Run.

The Xcode "Compile Kotlin Framework" build phase runs
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`, which links the shared `Shared`
framework. Targets: `iosArm64` (device) and `iosSimulatorArm64` (Apple Silicon simulator).

To verify the shared Kotlin code compiles for iOS without Xcode:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
