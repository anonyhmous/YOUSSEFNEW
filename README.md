# DriveFlow Driver (درايف فلو كابتن)

**DriveFlow Driver** is an Android driver assistant application engineered specifically for ride-hailing drivers in Egypt operating on **inDrive (`sinet.startup.inDriver`)**, **Uber Driver (`com.ubercab.driver`)**, and **DiDi Driver (`com.didiglobal.driver`)**.

It reads incoming trip requests in real-time, instantly evaluates the net earnings per kilometer based on the driver's custom economic rules (fuel cost, maintenance, commission, extra stops), and displays a clear color-coded verdict (🟢 Green / 🟠 Orange / 🔴 Red / ⚪ Gray) via non-intrusive floating badges and an intelligent summary bar.

> 🛡️ **Zero-Server & Read-Only Architecture:**
> - DriveFlow **NEVER** taps, clicks, accepts, declines, or counters offers. It strictly reads the screen and displays metrics.
> - DriveFlow **NEVER** uploads data to any server. There is no cloud backend, database, or analytics tracking.
> - DriveFlow **NEVER** stores passenger names, addresses, or phone numbers. All calculations are transient in device memory.

---

## 🚀 The Owner's Complete Order of Work

The entire commercial and operational ecosystem operates through 5 simple steps linking the single-file offline HTML Key Generator with this Android APK:

```
[ Step 1: Owner Key Tool ] -> Generate ECDSA P-256 Key Pair (keep Private Key safe!)
             │
             ▼
[ Step 2: Android Build ]  -> Paste SPKI Public Key into build configuration
             │
             ▼
[ Step 3: APK Release ]    -> Sign APK with consistent Keystore -> Build APK
             │
             ▼
[ Step 4: Distribution ]   -> Send APK to driver group via WhatsApp or GitHub Release
             │
             ▼
[ Step 5: Subscription ]   -> Driver pays on InstaPay (01122104999)
                           -> Driver sends Device Code + payment screenshot on WhatsApp
                           -> Owner issues offline key in Key Tool -> Sends key back
                           -> Driver pastes key into DriveFlow -> Instant ACTIVE status!
```

---

## 📱 Technology Stack
- **Platform**: Native Android (Kotlin, Jetpack Compose Material 3)
- **Minimum SDK**: 30 (Android 11) | **Target SDK**: 35 (Android 15)
- **Architecture**: MVVM, Kotlin Coroutines, StateFlow, Kotlinx Serialization, AndroidX DataStore
- **Dependency Injection**: Manual `AppGraph` singleton container (zero heavyweight reflection frameworks)
- **Screen Reading**: Android Accessibility Service Node Inspection & Selective OCR Cropping (Google ML Kit)
- **Floating Overlays**: Native `WindowManager` with lifecycle-aware ComposeViews (`FLAG_NOT_TOUCHABLE` for markers, touchable drag grip for DecisionBar)
- **Cryptography**: ECDSA P-256 with SHA-256 offline verification conforming strictly to the `LICENSE CONTRACT`

---

## 📂 Final Project File Tree

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/driveflow/driver/
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── di/
│   │   │   │   │   └── AppGraph.kt
│   │   │   │   ├── model/
│   │   │   │   │   └── Models.kt
│   │   │   │   └── util/
│   │   │   │       ├── LocaleHelper.kt
│   │   │   │       └── TextParsers.kt
│   │   │   ├── data/
│   │   │   │   └── prefs/
│   │   │   │       └── SettingsStore.kt
│   │   │   ├── engine/
│   │   │   │   ├── ArabicTextNormalizer.kt
│   │   │   │   ├── DecisionStore.kt
│   │   │   │   ├── HomeWayScorer.kt
│   │   │   │   ├── OpportunityDetector.kt
│   │   │   │   └── RulesEngine.kt
│   │   │   ├── license/
│   │   │   │   ├── DeviceHelper.kt
│   │   │   │   ├── LicenseGate.kt
│   │   │   │   ├── LicenseRepository.kt
│   │   │   │   └── OfflineLicenseVerifier.kt
│   │   │   ├── ocr/
│   │   │   │   ├── FakeOcrEngine.kt
│   │   │   │   ├── MlKitOcrEngine.kt
│   │   │   │   ├── OcrCalibrationManager.kt
│   │   │   │   ├── OcrEngine.kt
│   │   │   │   ├── OcrThrottleController.kt
│   │   │   │   └── ScreenshotSource.kt
│   │   │   ├── overlay/
│   │   │   │   ├── CardMarksOverlay.kt
│   │   │   │   ├── DecisionBarOverlay.kt
│   │   │   │   ├── OpportunityAlertManager.kt
│   │   │   │   ├── OverlayLifecycleOwner.kt
│   │   │   │   ├── OverlayManager.kt
│   │   │   │   └── OverlayPositionHelper.kt
│   │   │   ├── reader/
│   │   │   │   ├── didi/
│   │   │   │   │   └── DidiReader.kt
│   │   │   │   ├── indrive/
│   │   │   │   │   ├── InDriveReader.kt
│   │   │   │   │   └── InDriveTripDistanceOcr.kt
│   │   │   │   ├── model/
│   │   │   │   └── uber/
│   │   │   │       └── UberReader.kt
│   │   │   └── ui/
│   │   │       ├── navigation/
│   │   │       │   └── AppNavGraph.kt
│   │   │       ├── screens/
│   │   │       │   ├── alerts/AlertsScreen.kt
│   │   │       │   ├── areas/AreasScreen.kt
│   │   │       │   ├── calibration/CalibrationScreen.kt
│   │   │       │   ├── chat/ChatScreen.kt
│   │   │       │   ├── commitments/CommitmentsScreen.kt
│   │   │       │   ├── consent/ConsentScreen.kt
│   │   │       │   ├── contact/ContactScreen.kt
│   │   │       │   ├── diagnostics/DiagnosticsScreen.kt
│   │   │       │   ├── help/HelpScreen.kt
│   │   │       │   ├── home/HomeScreen.kt
│   │   │       │   ├── homeway/HomeWayScreen.kt
│   │   │       │   ├── rules/RulesScreen.kt
│   │   │       │   ├── settings/SettingsScreen.kt
│   │   │       │   ├── subscribe/SubscribeScreen.kt
│   │   │       │   └── welcome/WelcomeScreen.kt
│   │   │       └── theme/
│   │   │           ├── Color.kt
│   │   │           └── Theme.kt
│   │   └── res/
│   │       ├── raw/opportunity_chime.wav
│   │       ├── values/strings.xml
│   │       ├── values-en/strings.xml
│   │       ├── xml/backup_rules.xml
│   │       └── xml/data_extraction_rules.xml
│   └── test/
│       └── java/com/driveflow/driver/
│           ├── DidiReaderTest.kt
│           ├── FakeOcrEngineTest.kt
│           ├── InDriveTripDistanceOcrTest.kt
│           ├── LicenseVerificationContractTest.kt
│           ├── OcrThrottleControllerTest.kt
│           ├── OverlayManagerTest.kt
│           ├── OverlayPositionHelperTest.kt
│           ├── PolicySerializationTest.kt
│           ├── RulesEngineTest.kt
│           ├── SecurityHardeningTest.kt
│           ├── TextParsersTest.kt
│           └── UberReaderTest.kt
├── scripts/
│   └── bump_version.sh
├── .github/
│   └── workflows/build-release.yml
├── BUILD_PROPERTIES.md
├── CALIBRATION_PLAYBOOK.md
├── DRIVER_GUIDE.md
├── PRIVACY_AND_CONSENT.md
├── QA_CHECKLISTS.md
└── README.md
```

---

## 🛠️ Testing & Verification Commands

```bash
# Execute the entire automated unit test suite (100+ tests including contract vectors & security checks)
gradle :app:testDebugUnitTest

# Compile debug applet
gradle :app:assembleDebug

# Bump version
./scripts/bump_version.sh patch
```

---

## 📄 Documentation Index
- 📘 [Driver Quick Guide (دليل السائق)](DRIVER_GUIDE.md)
- 🔒 [Privacy & Consent Commitments (وثيقة الخصوصية والتعهدات)](PRIVACY_AND_CONSENT.md)
- 🎛️ [Calibration Playbook (دليل المعايرة الفنية لأوبر ودي دي)](CALIBRATION_PLAYBOOK.md)
- ✅ [QA & Cross-Check Checklists](QA_CHECKLISTS.md)
- 🔑 [Build Properties & Signing Guide](BUILD_PROPERTIES.md)


---

## Supported Android versions
Android 11, 12, 13, 14, 15 and 16 (API 30 to 36). Target SDK is 36.

On Android 13 and later, a sideloaded APK needs "Allow restricted settings" before Accessibility can be enabled:
Settings > Apps > DriveFlow > top-right menu (three dots) > Allow restricted settings, then enable the DriveFlow accessibility service.
