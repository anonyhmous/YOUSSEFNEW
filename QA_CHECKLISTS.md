# DriveFlow Driver - QA Checklists & Verification Procedures

This document provides mandatory Quality Assurance procedures for the owner and testers before issuing APK builds to drivers.

---

## Part 1: Offline License System Cross-Checks (Key Tool <-> Android App)

The Android app and the owner's key generator operate 100% offline linked solely by the cryptographic LICENSE CONTRACT (ECDSA P-256 with SHA-256).

| # | Test Scenario | Steps to Execute | Expected Behavior | Status |
|---|---------------|------------------|-------------------|--------|
| **1.1** | **Automated Contract Vectors** | Run unit tests via `gradle :app:testDebugUnitTest`. | All 7 test vectors in `LicenseVerificationContractTest` pass 100% (paid 30d, trial 2d, expired, wrong device, tampered signature, clock rollback, future issue). | [x] Passed |
| **1.2** | **Generate Real Key Pair** | Open owner's HTML Key Tool in browser. Generate new ECDSA key pair. Copy public key SPKI base64. | Public key is exported in standard SPKI Base64 format. | [ ] |
| **1.3** | **Embed Public Key in Build** | Set `LICENSE_PUBLIC_KEY` in `build.gradle.kts` / CI build properties. Assemble APK. | App builds cleanly with matching public key constant in `BuildConfig`. | [ ] |
| **1.4** | **Device Code Generation** | Install APK on test phone #1. Open app -> Subscription screen. | Displays formatted uppercase code (e.g., `ABCD-EF01-2345-6789-ABCD-EF01-2345-6789`). | [ ] |
| **1.5** | **Issue 30-Day Paid Key** | In Key Tool: paste device code from phone #1, select 30 Days (Plan 1), click "Generate Key". | Token starts with `DF1.` and has exactly 119 chars total. | [ ] |
| **1.6** | **Activate on Phone #1** | Paste key into phone #1 -> Tap Activate. | Status transitions to `ACTIVE`, shows 30 remaining days and correct expiration date. | [ ] |
| **1.7** | **Wrong Device Protection** | Install APK on test phone #2. Paste the key generated for phone #1. | App rejects key with error message `WRONG_DEVICE` and status remains `FREE`. | [ ] |
| **1.8** | **Clock Rollback Detection** | On activated phone #1, turn device clock back by 3 days in Android System Settings. | App detects rollback (`CLOCK_TAMPERED`), falls back to `FREE` daily quotas, and shows clock warning banner. | [ ] |
| **1.9** | **Clock Restoration** | Set phone #1 clock back to automatic network time. | High-water mark allows license to resume `ACTIVE` status without requiring re-activation. | [ ] |
| **1.10**| **Trial Key Expiration** | Issue 1-day Trial key (Plan 2). Advance device date by 2 days. | App gracefully expires key, transitions to `FREE` mode with 20 list reads / 5 detail reads daily. | [ ] |

---

## Part 2: Device Matrix & OS Manufacturer Checklist

Android OEM custom skins (Samsung One UI, Xiaomi HyperOS/MIUI, Oppo/Realme ColorOS) apply aggressive background process managers. Test each scenario on hardware before release.

### 2.1 Android OS Versions
- **Android 11 (API 30 - Minimum Supported SDK)**:
  - Accessibility Service registers and starts smoothly.
  - Overlay permission requested and granted via System Settings.
  - Text-to-speech chime and vibration alerts function normally.
- **Android 12 (API 31)**:
  - Material 3 dynamic theme adapts to wallpaper or default navy palette.
  - Backup rules exclusion verified.
- **Android 13 (API 33)**:
  - **"Restricted Settings" (الإعدادات المقيدة)**: Sideloaded APK will trigger Android 13 security prompt when enabling Accessibility. Verify driver guide walkthrough: Settings -> Apps -> DriveFlow Driver -> 3 dots top right -> "Allow restricted settings" -> enter phone PIN -> enable Accessibility.
  - Notification permission (`POST_NOTIFICATIONS`) prompt handled cleanly.
- **Android 14 (API 34)**:
  - Overlay window type `TYPE_APPLICATION_OVERLAY` displays without flickering.
  - Gesture navigation does not interfere with draggable bar.
- **Android 15 (API 35 - Target SDK)**:
  - Predictive back animation behaves properly.
  - Insets and edge-to-edge rendering align cards with bottom navigation bar.

### 2.2 OEM Specific Battery & Background Settings
- **Samsung One UI**:
  - Add DriveFlow Driver to "Never sleeping apps" (التطبيقات التي لا يتم وضعها في وضع السكون مطلقاً).
  - Enable "Appear on top" (الظهور في الأعلى).
- **Xiaomi MIUI / HyperOS**:
  - Enable "Autostart" (التشغيل التلقائي).
  - Set Battery Saver to "No restrictions" (لا توجد قيود).
  - In Permissions: enable "Display pop-up windows while running in the background".
- **Oppo / Realme ColorOS**:
  - Enable "Allow background activity".
  - Disable "Auto-freeze".

### 2.3 UX & Accessibility Edge Cases
- **Accessibility Service Recovery**: Force stop the app or terminate it in recents; verify that when the driver launches any ride app, the system AccessibilityService auto-reconnects without crashing.
- **RTL / LTR Language Switch**: Switch language from Arabic to English in Settings. All strings flip alignment cleanly, markers swap between left/right card anchors, and no text overlaps occur.
- **System Font Scaling (1.3x - 1.5x)**: Increase phone font size to Large in Android Display Settings. DecisionBar and Card Marks scale proportionately without clipping numbers or Arabic text.
- **Screen Rotation**: Rotate phone to landscape mode. Auto-placement calculation moves the floating bar beside the map route without obstructing driver action buttons.
- **Safe Kill Wrapper**: Inject an overlay exception; confirm overlay views dismiss cleanly, error counter increments in diagnostics, and the core accessibility reader remains running.


---

## Android 16 coverage
- [ ] Install and run on Android 11, 12, 13, 14, 15 and 16.
- [ ] Android 13+: notification permission prompt appears once and alerts work after allowing it.
- [ ] Android 13+: "Allow restricted settings" then enable the accessibility service.
- [ ] Android 15/16: edge-to-edge screens are not covered by the status/navigation bars.
- [ ] Android 16: back gesture works on all screens (predictive back).
