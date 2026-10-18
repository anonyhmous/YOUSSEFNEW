# DriveFlow Overlay Architecture (Prompt 6 of 8)

## 1. Overview
The overlay module (`com.driveflow.driver.overlay`) provides the visual indicators that assist drivers across inDrive, Uber Driver, and DiDi Driver:
1. **Card Marks (`CardMarksOverlay`)**: Non-touchable badges anchored directly to each ride request card, reflecting the verdict tone (Green, Orange, Red, Gray) and icon (eye for promising list cards, check, clock, cross, dots).
2. **Decision Bar (`DecisionBarOverlay`)**: Touch-enabled floating bar shown in detail mode (`isDetailOpen == true`), featuring drag grip, 4 metric tiles (Offer, Total km, Lowest fair price, Road condition), extra info line, flags row, heading-home indicator, Close, and Report buttons.
3. **Opportunity Alert (`OpportunityAlertManager`)**: High-importance notification channel with dedicated chime (`res/raw/opportunity_chime.wav`) and vibration pattern for golden trips, respecting silent modes and without covering the driver's screen.
4. **Overlay Manager (`OverlayManager`)**: Central coordinator that enforces screen safety, window context creation on API 30+, `OverlayVisibilityController` callbacks, and global error recovery.

## 2. Window Types & Touch Safety
- **Window Types**: Prefers `TYPE_APPLICATION_OVERLAY` when `Settings.canDrawOverlays` is granted; falls back gracefully to `TYPE_ACCESSIBILITY_OVERLAY` when invoked from the accessibility service.
- **Touch Pass-Through**:
  - `CardMarksOverlay` utilizes `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_NO_LIMITS`. Touches pass directly through to the ride app so drivers can tap cards without friction.
  - `DecisionBarOverlay` is touchable, allowing repositioning via drag grip and tapping Close/Report buttons.
- **Safety Boundary**: The overlay never draws over DriveFlow itself (`com.driveflow.driver`), and only draws when a supported ride app (`sinet.startup.inDriver`, `com.ubercab.driver`, `com.didiglobal.driver`) is in the foreground.

## 3. WindowManager & Compose Integration
To host Jetpack Compose (`ComposeView`) inside standalone `WindowManager` views, `OverlayLifecycleOwner` configures:
- `ViewTreeLifecycleOwner`
- `ViewTreeSavedStateRegistryOwner`
- `ViewTreeViewModelStoreOwner`
This ensures Compose lifecycle, effects, and recompositions execute cleanly without leaks or crashes.

## 4. Positioning & Font Scaling
- `OverlayPositionHelper.calculateAutoPosition`: Avoids the top map area and the bottom 18% of the screen (Accept/Decline buttons) to prevent interference with driver actions.
- `OverlayPositionHelper.calculateScaledMarkerSizePx`: Scales Small (24dp), Normal (32dp), and Large (44dp) badges by system `fontScale` and display density.
- Stale mark eviction after 400ms prevents ghost markers when cards scroll or disappear.
