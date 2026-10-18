package com.driveflow.driver.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import com.driveflow.driver.core.di.AppGraph
import com.driveflow.driver.core.model.LicenseStatus
import com.driveflow.driver.core.model.ParsedRequest
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.core.model.Verdict
import com.driveflow.driver.engine.RulesEngine
import com.driveflow.driver.license.DeviceHelper
import com.driveflow.driver.core.di.LicenseVerificationResult as VerifyResult
import com.driveflow.driver.ocr.CaptureResult
import com.driveflow.driver.ocr.CompositeOcrEngine
import com.driveflow.driver.ocr.OcrEngine
import com.driveflow.driver.ocr.OcrThrottleController
import com.driveflow.driver.ocr.ScreenshotSource
import com.driveflow.driver.overlay.OverlayManager
import com.driveflow.driver.reader.didi.DidiReader
import com.driveflow.driver.reader.indrive.InDriveReader
import com.driveflow.driver.reader.indrive.InDriveTripDistanceOcr
import com.driveflow.driver.reader.uber.UberReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * خدمة إمكانية الوصول: بتقرا شاشة تطبيقات inDrive / Uber / DiDi (قراءة فقط)
 * وبتعرض النتيجة على الـ Overlay. مبتضغطش ولا بتقبل ولا بترفض أي طلب.
 */
class DriveFlowAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val processMutex = Mutex()

    private var overlayManager: OverlayManager? = null
    private var ocrEngine: OcrEngine? = null
    private var screenshotSource: ScreenshotSource? = null
    private var throttle: OcrThrottleController? = null
    private var inDriveReader: InDriveReader? = null
    private var uberReader: UberReader? = null
    private var didiReader: DidiReader? = null

    private var tickerJob: Job? = null
    private var debounceJob: Job? = null

    @Volatile
    private var foregroundPackage: String? = null

    @Volatile
    private var lastScreenBounds: Rect = Rect(0, 0, 1080, 2400)

    private val ignoredPackages = setOf(
        "com.android.systemui",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.sec.android.inputmethod"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AppGraph.init(this)

        val engine = CompositeOcrEngine(this)
        ocrEngine = engine
        AppGraph.ocrEngine = engine

        throttle = OcrThrottleController(applicationContext)
        val shots = ScreenshotSource { this }
        screenshotSource = shots

        val overlay = OverlayManager(this, isAccessibilityService = true)
        overlayManager = overlay
        overlay.start()

        val tripOcr = InDriveTripDistanceOcr(
            screenshotSource = shots,
            ocrEngine = engine,
            overlayController = overlay,
            screenBoundsProvider = { lastScreenBounds }
        )
        inDriveReader = InDriveReader(applicationContext, tripOcr)
        uberReader = UberReader(engine)
        didiReader = DidiReader(engine)

        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(TICK_MS)
                if (isRideApp(foregroundPackage)) safeProcess()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        // نتجاهل أحداث الـ Overlay بتاعنا والكيبورد وشريط النظام
        if (pkg in ignoredPackages) return
        if (pkg == SELF_PACKAGE && event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            foregroundPackage = pkg
        }

        if (!isRideApp(foregroundPackage)) {
            overlayManager?.hideAll()
            return
        }

        // Debounce: نستنى 300 مللي ثانية بعد آخر تغيير في الشاشة
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(DEBOUNCE_MS)
            safeProcess()
        }
    }

    override fun onInterrupt() {
        overlayManager?.hideAll()
    }

    override fun onDestroy() {
        instance = null
        tickerJob?.cancel()
        debounceJob?.cancel()
        overlayManager?.stop()
        try {
            ocrEngine?.close()
        } catch (_: Exception) {
        }
        AppGraph.ocrEngine = null
        scope.cancel()
        super.onDestroy()
    }

    private fun isRideApp(pkg: String?): Boolean =
        pkg != null && RideApp.values().any { it.pkg == pkg }

    private suspend fun safeProcess() {
        try {
            process()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            overlayManager?.hideAll()
            AppGraph.settingsStore.incrementDiagnosticCounter("service_error")
        }
    }

    private suspend fun process() = processMutex.withLock {
        val pkg = foregroundPackage
        val app = RideApp.values().firstOrNull { it.pkg == pkg }
        val overlay = overlayManager
        if (app == null || overlay == null) {
            overlay?.hideAll()
            return@withLock
        }

        val store = AppGraph.settingsStore
        // بوابة الموافقة: مفيش قراءة قبل ما السواق يوافق
        if (store.getConsentVersion() < 1) {
            overlay.hideAll()
            return@withLock
        }
        val policy = store.getPolicy()
        if (policy.appEnabled[app] == false) {
            overlay.hideAll()
            return@withLock
        }

        val root = rootInActiveWindow
        val screen = Rect()
        root?.getBoundsInScreen(screen)
        if (screen.width() <= 0 || screen.height() <= 0) {
            screen.set(lastScreenBounds)
        } else {
            lastScreenBounds = Rect(screen)
        }

        var mapBounds: Rect? = null
        val requests: List<ParsedRequest> = when (app) {
            RideApp.INDRIVE -> {
                val reader = inDriveReader
                if (root == null || reader == null) {
                    emptyList()
                } else {
                    mapBounds = reader.mapBounds(root)
                    val detail = reader.readDetail(root)
                    val list = reader.readList(root)
                    if (detail != null) {
                        listOf(detail) + list.filter { it.key != detail.key }
                    } else {
                        list
                    }
                }
            }
            RideApp.UBER, RideApp.DIDI -> {
                val parsed = readByOcr(app, pkg, screen)
                    ?: run {
                        // لو الشاشة متغيرتش أو الـ throttle مسمحش، نسيب العرض زي ما هو
                        return@withLock
                    }
                parsed
            }
        }

        val verdicts = HashMap<String, Verdict>()
        for (request in requests) {
            val verdict = RulesEngine.evaluate(request, policy) ?: continue
            verdicts[request.key] = verdict
            AppGraph.opportunityDetector.inspect(request, verdict)
        }

        overlay.onScreenContentChanged(
            currentForegroundPackage = pkg,
            requests = requests,
            verdicts = verdicts,
            screenBounds = screen,
            mapBounds = mapBounds,
            licenseStatus = currentLicenseStatus(),
            readerActive = true
        )
    }

    /**
     * يرجّع null لو مفيش قراءة جديدة (throttle أو الشاشة ثابتة)،
     * أو قائمة (ممكن تكون فاضية) لو حصلت قراءة.
     */
    private suspend fun readByOcr(app: RideApp, pkg: String?, screen: Rect): List<ParsedRequest>? {
        val gate = throttle ?: return null
        val shots = screenshotSource ?: return null
        val engine = ocrEngine ?: return null
        if (!gate.shouldProcess()) return null

        val capture = shots.capture(pkg)
        if (capture !is CaptureResult.Success) return null
        val bitmap = capture.bitmap
        try {
            if (!gate.checkAndRecordContentChanged(gate.computeBitmapHash(bitmap))) return null
            gate.recordOcrProcessed()

            val region = AppGraph.ocrCalibrationManager.getRegion(app, "offer_card")
            val ocr = engine.read(bitmap, region)
            val bounds = Rect(0, 0, bitmap.width, bitmap.height)
            val parsed = when (app) {
                RideApp.UBER -> uberReader?.parseOcrResult(ocr, bounds)
                RideApp.DIDI -> didiReader?.parseOcrResult(ocr, bounds)
                else -> null
            }
            return if (parsed != null) listOf(parsed) else emptyList()
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private suspend fun currentLicenseStatus(): LicenseStatus {
        val store = AppGraph.settingsStore
        val token = store.getLicenseToken()
        if (token.isNullOrBlank()) return LicenseStatus.FREE
        val result = AppGraph.licenseVerifier.verify(
            token = token,
            publicKeySpkiB64 = AppGraph.licensePublicKey,
            deviceIdHex = DeviceHelper.getDeviceIdHex(this),
            currentDay = AppGraph.clock.currentUtcDay(),
            clockHighWaterDay = store.getClockHighWaterDay()
        )
        return if (result is VerifyResult.Valid) result.status else LicenseStatus.FREE
    }

    companion object {
        private const val SELF_PACKAGE = "com.driveflow.driver"
        private const val DEBOUNCE_MS = 300L
        private const val TICK_MS = 1000L

        @Volatile
        var instance: DriveFlowAccessibilityService? = null
            private set
    }
}
