# DocScanner

DocScanner is a native **Kotlin Multiplatform (KMP)** application targeting Android and iOS. It is designed to capture physical handwritten documents, manage translation projects, render pages to images, and send them to a Cloudflare Workers backend for **Handwritten Text Recognition (HTR)** transcription using state-of-the-art AI models.

---

## 🚀 Key Features

### 1. Native Document Scanning
The app uses platform-specific system APIs to scan multi-page physical documents with auto-detection, cropping, and color filtering:
*   **Android**: Integrates the Google Code Scanner via ML Kit's **Google Play Services Document Scanner** (`play-services-mlkit-document-scanner`), supporting page limits up to 1000, importing from the system gallery, and direct PDF generation.
*   **iOS**: Integrates Apple's native **VisionKit** framework (`VNDocumentCameraViewController`), allowing high-quality scans on supported iOS devices.

### 2. High-Quality PDF Rendering to Images
Scanned documents are rendered locally on-device into individual page images (JPEG) for review and upload:
*   **Android**: Uses the native Android [PdfRenderer](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/androidMain/kotlin/org/bibletranslationtools/docscanner/platform/Platform.android.kt#L122) to convert PDF pages to bitmap files.
*   **iOS**: Uses iOS's [PDFDocument](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/iosMain/kotlin/org/bibletranslationtools/docscanner/platform/Platform.ios.kt#L181) (PDFKit) and CoreGraphics thumbnails to extract page-by-page images.

### 3. Local SQLite Storage & Caching
Maintains offline-first capabilities using **SQLDelight**:
*   Stores information regarding **Languages** (`LanguageEntity`), **Books** (`BookEntity`), **Levels** (`LevelEntity`), **Projects** (`ProjectEntity`), and generated **PDFs** (`PdfEntity`).
*   Schema definitions are defined in [Database.sq](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/sqldelight/org/bibletranslationtools/database/Database.sq), [Project.sq](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/sqldelight/org/bibletranslationtools/database/Project.sq), and [Pdf.sq](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/sqldelight/org/bibletranslationtools/database/Pdf.sq).
*   Manages user preferences and settings via [PreferencesRepository](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/kotlin/org/bibletranslationtools/docscanner/data/repository/PreferencesRepository.kt) backed by Multiplatform Settings.

### 4. Handwritten Text Recognition (HTR) Sync & API
An integrated Ktor HTTP client communicates with a custom Cloudflare Workers API server:
*   **Authentication**: Secure login/logout via username/password at `/auth/login` with session storage handled by a [PersistentCookieStorage](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/kotlin/org/bibletranslationtools/docscanner/api/PersistentCookieStorage.kt).
*   **Transcription Upload**: Base64-encodes JPEGs and uploads them with metadata (language, book, level, chapter number, timestamp).
*   **AI Transcription Engine**: Support for selecting transcription models such as OpenAI and Pixtral, retrieving the final transcribed text.
*   See [TranscriberApi](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/kotlin/org/bibletranslationtools/docscanner/api/TranscriberApi.kt) for details.

### 5. Native Sharing & Previewing
Enables backup and previews of scan documents natively:
*   **Android**: Uses `Intent.ACTION_SEND`/`Intent.ACTION_VIEW` and a `FileProvider` to share or open PDFs, and the `kzip` compression library to zip whole project directories.
*   **iOS**: Uses `UIActivityViewController` and QuickLook (`QLPreviewController`) to preview and share documents, and `NSFileCoordinator` for folder zipping.

---

## 🛠 Tech Stack & Libraries
*   **Shared UI**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Jetpack Compose for cross-platform layouts)
*   **Navigation**: Voyager (Screens, view models, and transitions)
*   **Dependency Injection**: Koin (KMP-compatible DI)
*   **HTTP Client**: Ktor (Client Engine with JSON negotiation and cookie support)
*   **Database**: SQLDelight (Multiplatform SQLite wrapper)
*   **JSON Serialization**: Kotlinx Serialization JSON
*   **I/O**: Kotlinx IO (File system access)
*   **DateTime**: Kotlinx DateTime
*   **Logging**: Kotlin Logging (with SLF4J / Logback-android)

---

## 📂 Project Structure

*   [`/shared`](file:///Users/mxaln/StudioProjects/DocScanner/shared) is the Compose Multiplatform module containing code shared across platforms:
    *   [`commonMain`](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/commonMain/kotlin/org/bibletranslationtools/docscanner) — Core shared UI (screens, viewmodels, themes), repositories, model objects, database queries, and Ktor client integrations.
    *   [`androidMain`](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/androidMain/kotlin/org/bibletranslationtools/docscanner) — Android-specific actual implementations (GMS scanning launcher, PdfRenderer wrapper, Intent-based file sharer).
    *   [`iosMain`](file:///Users/mxaln/StudioProjects/DocScanner/shared/src/iosMain/kotlin/org/bibletranslationtools/docscanner) — iOS-specific actual implementations (VisionKit scanner, PDFKit document/thumbnail extractor, UIActivity/QuickLook sharing).
*   [`/androidApp`](file:///Users/mxaln/StudioProjects/DocScanner/androidApp) is the Android application entry point (`com.android.application`).
*   [`/iosApp`](file:///Users/mxaln/StudioProjects/DocScanner/iosApp) is the Xcode project for the iOS application, hosting the shared Compose UI via `MainViewController`.

---

## 🏗 Building & Running

### Android
To build the debug APK:
```bash
./gradlew :androidApp:assembleDebug
```

### iOS
Requires **full Xcode** installed (not just Xcode Command Line Tools).

1. Open `iosApp/iosApp.xcodeproj` or `iosApp/iosApp.xcworkspace` in Xcode.
2. Set your signing **Team** (in *Signing & Capabilities*), or specify your `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.
3. Select an iOS Simulator or a connected device and run.

The Xcode "Compile Kotlin Framework" build phase runs:
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```
This links the shared Kotlin framework (`shared.framework`) targeting `iosArm64` (devices) and `iosSimulatorArm64` (simulators).

To verify the Kotlin shared code compiles for iOS without running Xcode:
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```
