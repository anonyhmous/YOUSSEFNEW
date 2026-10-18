package com.driveflow.driver.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveflow.driver.core.model.ParsedRequest
import com.driveflow.driver.core.model.RoadState
import com.driveflow.driver.core.model.Tone
import com.driveflow.driver.core.model.Verdict
import com.driveflow.driver.data.prefs.BarPosition
import com.driveflow.driver.ui.theme.BrandGold
import com.driveflow.driver.ui.theme.BrandMint
import com.driveflow.driver.ui.theme.BrandNavyBackground
import com.driveflow.driver.ui.theme.BrandNavySurface
import com.driveflow.driver.ui.theme.TextMuted
import com.driveflow.driver.ui.theme.TextWhite
import com.driveflow.driver.ui.theme.VerdictGray
import com.driveflow.driver.ui.theme.VerdictGreen
import com.driveflow.driver.ui.theme.VerdictOrange
import com.driveflow.driver.ui.theme.VerdictRed
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import kotlin.math.roundToInt

data class DecisionBarState(
    val request: ParsedRequest? = null,
    val verdict: Verdict? = null,
    val opacityPercent: Int = 67,
    val isRtl: Boolean = true,
    val headingHomeInfo: String? = null,
    val isVisible: Boolean = false
)

/**
 * Overlay hosting the detailed DecisionBar when isDetailOpen == true.
 * Touch-enabled with drag grip, metric tiles, Close button, and Report button.
 */
class DecisionBarOverlay(
    private val context: Context,
    private val windowManager: WindowManager,
    private val windowType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
    },
    private val onReportClicked: (ParsedRequest, Verdict) -> Unit = { _, _ -> },
    private val onPositionSaved: (BarPosition) -> Unit = {}
) {
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var isAttached = false

    private val stateFlow = MutableStateFlow(DecisionBarState())
    private var dismissedRequestKey: String? = null

    var barPosition: BarPosition = BarPosition(isAuto = true)
    var isRtl: Boolean = true

    fun attach() {
        if (isAttached) return

        val view = ComposeView(context).apply {
            setContent {
                val state by stateFlow.collectAsState()
                if (state.isVisible && state.request != null && state.verdict != null) {
                    DecisionBarCard(
                        request = state.request!!,
                        verdict = state.verdict!!,
                        opacityPercent = state.opacityPercent,
                        isRtl = state.isRtl,
                        headingHomeInfo = state.headingHomeInfo,
                        onClose = {
                            dismissCurrent()
                        },
                        onReport = {
                            onReportClicked(state.request!!, state.verdict!!)
                        },
                        onDrag = { dx, dy ->
                            moveBy(dx, dy)
                        },
                        onDragEnd = {
                            saveCustomPosition()
                        }
                    )
                }
            }
        }

        val owner = OverlayLifecycleOwner()
        owner.attachToView(view)
        this.lifecycleOwner = owner
        this.composeView = view

        val density = context.resources.displayMetrics.density
        val barWidthPx = (340 * density).toInt()

        val params = WindowManager.LayoutParams(
            barWidthPx,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        this.windowParams = params

        try {
            windowManager.addView(view, params)
            isAttached = true
        } catch (e: Exception) {
            owner.destroy()
            this.composeView = null
            this.lifecycleOwner = null
            throw e
        }
    }

    fun detach() {
        if (!isAttached) return
        composeView?.let { view ->
            try {
                windowManager.removeViewImmediate(view)
            } catch (_: Exception) {}
        }
        lifecycleOwner?.destroy()
        composeView = null
        lifecycleOwner = null
        windowParams = null
        isAttached = false
    }

    /**
     * Updates detail request and recalculates position.
     */
    fun show(
        request: ParsedRequest,
        verdict: Verdict,
        opacityPercent: Int,
        screenBounds: Rect,
        mapBounds: Rect?,
        headingHomeInfo: String? = null
    ) {
        if (request.key == dismissedRequestKey) {
            return
        }

        stateFlow.value = DecisionBarState(
            request = request,
            verdict = verdict,
            opacityPercent = opacityPercent,
            isRtl = isRtl,
            headingHomeInfo = headingHomeInfo,
            isVisible = true
        )

        updatePosition(screenBounds, mapBounds)
    }

    fun hide() {
        stateFlow.value = stateFlow.value.copy(isVisible = false)
    }

    fun dismissCurrent() {
        val currentKey = stateFlow.value.request?.key
        if (currentKey != null) {
            dismissedRequestKey = currentKey
        }
        hide()
    }

    private fun updatePosition(screenBounds: Rect, mapBounds: Rect?) {
        val params = windowParams ?: return
        val view = composeView ?: return

        val density = context.resources.displayMetrics.density
        val barWidthPx = params.width.coerceAtLeast((300 * density).toInt())
        val barHeightPx = view.height.coerceAtLeast((220 * density).toInt())

        if (barPosition.isAuto) {
            val autoPoint = OverlayPositionHelper.calculateAutoPosition(
                screenBounds = screenBounds,
                mapBounds = mapBounds,
                barWidthPx = barWidthPx,
                barHeightPx = barHeightPx,
                isLandscape = screenBounds.width() > screenBounds.height(),
                density = density
            )
            params.x = autoPoint.x
            params.y = autoPoint.y
        } else {
            params.x = barPosition.x
            params.y = barPosition.y
        }

        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun moveBy(dx: Float, dy: Float) {
        val params = windowParams ?: return
        val view = composeView ?: return

        params.x = (params.x + dx.roundToInt()).coerceAtLeast(0)
        params.y = (params.y + dy.roundToInt()).coerceAtLeast(0)

        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun saveCustomPosition() {
        val params = windowParams ?: return
        barPosition = BarPosition(isAuto = false, x = params.x, y = params.y)
        onPositionSaved(barPosition)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DecisionBarCard(
    request: ParsedRequest,
    verdict: Verdict,
    opacityPercent: Int,
    isRtl: Boolean,
    headingHomeInfo: String?,
    onClose: () -> Unit,
    onReport: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val toneColor = when (verdict.tone) {
        Tone.GREEN -> VerdictGreen
        Tone.ORANGE -> VerdictOrange
        Tone.RED -> VerdictRed
        Tone.GRAY -> VerdictGray
    }

    val alpha = (opacityPercent.coerceIn(10, 100) / 100f)
    val cardBackground = BrandNavySurface.copy(alpha = alpha)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, toneColor, RoundedCornerShape(16.dp)),
        color = cardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Drag Grip Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.x, dragAmount.y)
                            },
                            onDragEnd = onDragEnd
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(TextMuted.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(toneColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRtl) verdict.titleAr else verdict.titleEn,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Report Button
                    IconButton(
                        onClick = onReport,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report Request",
                            tint = BrandGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Bar",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metric Tiles (4 Columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTile(
                    label = if (isRtl) "العرض" else "Offer",
                    value = formatFareValue(request.fare, request.currency, isRtl),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = if (isRtl) "المسافة" else "Total",
                    value = formatKmValue(verdict.totalKm, isRtl),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = if (isRtl) "أدنى سعر" else "Lowest",
                    value = formatLowestPrice(verdict.suggestedPrice, isRtl),
                    modifier = Modifier.weight(1f),
                    highlightColor = BrandGold
                )
                MetricTile(
                    label = if (isRtl) "الطريق" else "Road",
                    value = formatRoadState(request.roadState, isRtl),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra Info Row (Stops, Rating, Payment)
            val extraDetails = buildExtraInfo(request, isRtl)
            if (extraDetails.isNotBlank()) {
                Text(
                    text = extraDetails,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Flags Row
            if (verdict.flags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (flag in verdict.flags) {
                        FlagChip(flag = flag)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Heading-Home Line
            if (!headingHomeInfo.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏠 $headingHomeInfo",
                        color = BrandMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Reason Line
            val reasonDetail = if (isRtl) verdict.detailAr else verdict.detailEn
            if (reasonDetail.isNotBlank()) {
                Text(
                    text = reasonDetail,
                    color = TextWhite,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Retry Line when figures incomplete
            if (verdict.reasonCode == "detail_incomplete") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRtl) "أبقِ الطلب مفتوحاً لقراءة الخريطة" else "Keep request open to read map figures",
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = TextWhite
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandNavyBackground.copy(alpha = 0.7f))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )
            Text(
                text = value,
                color = highlightColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FlagChip(flag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BrandNavyBackground.copy(alpha = 0.8f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = flag,
            color = BrandGold,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatFareValue(fare: Double?, currency: String?, isRtl: Boolean): String {
    if (fare == null) return "—"
    val curr = currency ?: "EGP"
    return if (isRtl) "${fare.toInt()} ج" else "${fare.toInt()} $curr"
}

private fun formatKmValue(km: Double?, isRtl: Boolean): String {
    if (km == null) return "—"
    return String.format(Locale.US, "%.1f %s", km, if (isRtl) "كم" else "km")
}

private fun formatLowestPrice(suggested: Double?, isRtl: Boolean): String {
    if (suggested == null) return "—"
    return if (isRtl) "${suggested.toInt()} ج" else "${suggested.toInt()} EGP"
}

private fun formatRoadState(state: RoadState, isRtl: Boolean): String = when (state) {
    RoadState.EASY -> if (isRtl) "سالك" else "Easy"
    RoadState.SLOW -> if (isRtl) "بطيء" else "Slow"
    RoadState.HARD -> if (isRtl) "زحام" else "Hard"
    RoadState.UNKNOWN -> if (isRtl) "عادي" else "Normal"
}

private fun buildExtraInfo(request: ParsedRequest, isRtl: Boolean): String {
    val items = mutableListOf<String>()
    if (request.extraStops > 0) {
        items.add(if (isRtl) "${request.extraStops} وقفات" else "${request.extraStops} stops")
    }
    if (!request.paymentMethod.isNullOrBlank()) {
        items.add(request.paymentMethod)
    }
    if (request.riderRating != null) {
        items.add("★ ${String.format(Locale.US, "%.2f", request.riderRating)}")
    }
    if (request.riderTrips != null && request.riderTrips > 0) {
        items.add(if (isRtl) "${request.riderTrips} رحلة" else "${request.riderTrips} trips")
    }
    return items.joinToString(" • ")
}
