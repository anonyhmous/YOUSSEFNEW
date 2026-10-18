# DriveFlow Contract and Project Specification

## PROJECT FACTS
- Product: DriveFlow. THIS SERIES BUILDS ONLY THE ANDROID APP "DriveFlow Driver". The owner's key generator is a SEPARATE single-file tool built from the other prompt file. The two programs are linked ONLY by the LICENSE CONTRACT below (device code, key format, signature, test vectors). There is no server.
- Purpose: the Android app reads the screen of three driver apps (inDrive, Uber Driver, DiDi Driver) and shows a colored verdict beside each request and in a floating bar. It NEVER taps, accepts, rejects or negotiates. It only reads and shows.
- Distribution: NOT on Google Play. The APK is sent to a closed group of drivers. A driver subscribes by paying the owner on InstaPay (01122104999), then sends on WhatsApp (01128440045) his device code and a screenshot of the payment. The owner issues an offline key with his key tool and sends it back; the driver pastes it in the app.
- Android: Kotlin, Jetpack Compose Material3, applicationId com.driveflow.driver, minSdk 30, targetSdk 35, MVVM, coroutines/Flow, DataStore, kotlinx.serialization. No DI framework (manual AppGraph object). No networking library: the only network use is the optional Geocoder for the heading-home feature and opening WhatsApp links.
- Languages: Arabic (Egyptian dialect, RTL) is the default, English is second. All UI text lives in string resources.
- Brand colors: background navy #07131B, surface #0E2230, gold #F6C945, mint #7CF5C4, text #FFFFFF, muted text #9FB3C2. Verdict colors: GREEN #22C55E, ORANGE #F59E0B, RED #EF4444, GRAY #64748B.
- Supported ride apps and packages: inDrive = sinet.startup.inDriver, Uber Driver = com.ubercab.driver, DiDi Driver = com.didiglobal.driver. Internal codes: indrive, uber, didi.
- License statuses used everywhere: ACTIVE, TRIAL, FREE.
- Package names: com.driveflow.driver with sub-packages core, data, reader, ocr, engine, overlay, license, ui.

## SHARED KOTLIN MODELS
```kotlin
package com.driveflow.driver.core.model

enum class RideApp(val pkg: String, val code: String) { INDRIVE("sinet.startup.inDriver", "indrive"), UBER("com.ubercab.driver", "uber"), DIDI("com.didiglobal.driver", "didi") }
enum class ReadSource { ACCESSIBILITY, OCR }
enum class RoadState { UNKNOWN, EASY, SLOW, HARD }
enum class Tone { GREEN, ORANGE, RED, GRAY }
enum class StopsMode { IGNORE, PREFER, WARN, AVOID }
enum class LongTripMode { IGNORE, WARN, AVOID }
enum class DistanceUnit { KM, MILES, BOTH }
enum class LicenseStatus { ACTIVE, TRIAL, FREE }

data class ParsedRequest(
  val app: RideApp, val key: String, val source: ReadSource,
  val fare: Double?, val currency: String?,
  val pickupKm: Double?, val tripKm: Double?, val totalKm: Double?, val pickupMinutes: Int?,
  val riderRating: Double?, val riderTrips: Int?, val requestAgeSeconds: Int?,
  val pickupAddress: String?, val destinationAddress: String?, val extraStops: Int,
  val paymentMethod: String?, val riderNote: String?, val offerCount: Int?,
  val roadState: RoadState, val productName: String?,
  val bounds: android.graphics.Rect, val isDetailOpen: Boolean, val confidence: Float)

data class Policy(
  val minPricePerKm: Double = 0.0,
  val minTotalKm: Double? = null, val maxTotalKm: Double? = null, val maxTripKm: Double? = null, val maxPickupKm: Double? = null, val maxPickupMinutes: Int? = null,
  val minRiderRating: Double? = null, val minRiderTrips: Int? = null, val maxRequestAgeMinutes: Int? = null,
  val negotiationStep: Int = 5, val maxOffer: Double? = null,
  val currency: String = "EGP", val governorate: String? = null,
  val avoidedAreas: List<String> = emptyList(), val preferredAreas: List<String> = emptyList(),
  val allowedPayments: Set<String> = emptySet(), val bannedNoteWords: List<String> = emptyList(),
  val stopsMode: StopsMode = StopsMode.IGNORE, val stopFee: Double? = null,
  val longTripMode: LongTripMode = LongTripMode.IGNORE, val longTripKm: Double? = null,
  val warnOnUberSaver: Boolean = false, val freePickupKm: Double? = null,
  val fuelCostPerKm: Double? = null, val upkeepCostPerKm: Double? = null,
  val commissionPct: Map<RideApp, Double> = emptyMap(), val commissionEnabled: Map<RideApp, Boolean> = emptyMap(), val appEnabled: Map<RideApp, Boolean> = mapOf(RideApp.INDRIVE to true, RideApp.UBER to true, RideApp.DIDI to true),
  val distanceUnit: DistanceUnit = DistanceUnit.KM, val basisNet: Boolean = false)
// defaults: currency "EGP", negotiationStep 5, all appEnabled true, distanceUnit KM, basisNet false, stopsMode IGNORE, longTripMode IGNORE.
// carCostPerKm = (fuelCostPerKm ?: 0.0) + (upkeepCostPerKm ?: 0.0)

data class Verdict(
  val tone: Tone, val reasonCode: String,
  val titleAr: String, val titleEn: String, val detailAr: String, val detailEn: String,
  val flags: List<String>, val totalKm: Double?, val grossPerKm: Double?, val netPerKm: Double?,
  val netProfit: Double?, val suggestedPrice: Double?)

interface AppReader {
  val app: RideApp
  fun isRideAppWindow(root: android.view.accessibility.AccessibilityNodeInfo): Boolean
  fun readList(root: android.view.accessibility.AccessibilityNodeInfo): List<ParsedRequest>
  fun readDetail(root: android.view.accessibility.AccessibilityNodeInfo): ParsedRequest?
  fun mapBounds(root: android.view.accessibility.AccessibilityNodeInfo): android.graphics.Rect?
}

data class TripKmResult(val value: Double?, val failure: String?)
interface TripDistanceOcr { suspend fun readTripKm(mapBounds: android.graphics.Rect): TripKmResult }
```
