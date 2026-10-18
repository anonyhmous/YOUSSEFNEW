package com.driveflow.driver

import android.graphics.Point
import android.graphics.Rect
import com.driveflow.driver.data.prefs.MarkerSize
import com.driveflow.driver.overlay.OverlayPositionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OverlayPositionHelperTest {

    private val screenBounds = Rect(0, 0, 1080, 2400)

    @Test
    fun testAutoPlacementNeverOverlapsMapBoundsInPortrait() {
        // Map covers top 40% (0 to 960)
        val mapBounds = Rect(0, 0, 1080, 960)
        val barWidthPx = 800
        val barHeightPx = 500

        val pos = OverlayPositionHelper.calculateAutoPosition(
            screenBounds = screenBounds,
            mapBounds = mapBounds,
            barWidthPx = barWidthPx,
            barHeightPx = barHeightPx,
            isLandscape = false
        )

        val barRect = Rect(pos.x, pos.y, pos.x + barWidthPx, pos.y + barHeightPx)

        // 1. Bar must never overlap map bounds
        val overlapsMap = Rect.intersects(barRect, mapBounds)
        assertFalse("Bar must not overlap map bounds", overlapsMap)

        // 2. Bar must not cover the bottom 18% accept/decline action buttons
        val bottomActionButtonsTop = (screenBounds.height() * (1f - 0.18f)).toInt()
        assertTrue(
            "Bar bottom (${barRect.bottom}) should not intrude into bottom buttons ($bottomActionButtonsTop)",
            barRect.bottom <= bottomActionButtonsTop + 10
        )
    }

    @Test
    fun testAutoPlacementLandscapePlacesBesideMap() {
        val landscapeScreen = Rect(0, 0, 2400, 1080)
        // Map is on left half: 0 to 1200
        val mapBounds = Rect(0, 0, 1200, 1080)
        val barWidthPx = 800
        val barHeightPx = 500

        val pos = OverlayPositionHelper.calculateAutoPosition(
            screenBounds = landscapeScreen,
            mapBounds = mapBounds,
            barWidthPx = barWidthPx,
            barHeightPx = barHeightPx,
            isLandscape = true
        )

        val barRect = Rect(pos.x, pos.y, pos.x + barWidthPx, pos.y + barHeightPx)

        // Bar must be on the right side of the map
        assertTrue("Bar should be placed to the right of the map", barRect.left >= mapBounds.right)
        assertFalse("Bar must not overlap map in landscape", Rect.intersects(barRect, mapBounds))
    }

    @Test
    fun testFontScaleScalingForMarkers() {
        val density = 2.0f // 2x density (e.g. xhdpi)

        // Base dp: Small = 24, Normal = 32, Large = 44
        // At 1.0x fontScale:
        val small1x = OverlayPositionHelper.calculateScaledMarkerSizePx(MarkerSize.SMALL, density, 1.0f)
        val normal1x = OverlayPositionHelper.calculateScaledMarkerSizePx(MarkerSize.NORMAL, density, 1.0f)
        val large1x = OverlayPositionHelper.calculateScaledMarkerSizePx(MarkerSize.LARGE, density, 1.0f)

        assertEquals(48, small1x) // 24 * 2
        assertEquals(64, normal1x) // 32 * 2
        assertEquals(88, large1x) // 44 * 2

        // At 1.5x fontScale:
        val normal1_5x = OverlayPositionHelper.calculateScaledMarkerSizePx(MarkerSize.NORMAL, density, 1.5f)
        assertEquals(96, normal1_5x) // 32 * 2 * 1.5 = 96
        assertTrue(normal1_5x > normal1x)

        // Font scale clamped to minimum 16px
        val minScale = OverlayPositionHelper.calculateScaledMarkerSizePx(MarkerSize.SMALL, 0.5f, 0.5f)
        assertTrue(minScale >= 16)
    }

    @Test
    fun testAnchorPositioningRtlVsLtr() {
        val cardBounds = Rect(100, 500, 980, 800)
        val markerSizePx = 64
        val margin = 8

        // RTL: Anchors to left edge
        val rtlBounds = OverlayPositionHelper.calculateMarkerBounds(
            cardBounds = cardBounds,
            markerSizePx = markerSizePx,
            isRtl = true,
            screenBounds = screenBounds,
            marginPx = margin
        )

        assertNotNull(rtlBounds)
        assertEquals(cardBounds.left + margin, rtlBounds!!.left)
        assertEquals(rtlBounds.left + markerSizePx, rtlBounds.right)

        // LTR: Anchors to right edge
        val ltrBounds = OverlayPositionHelper.calculateMarkerBounds(
            cardBounds = cardBounds,
            markerSizePx = markerSizePx,
            isRtl = false,
            screenBounds = screenBounds,
            marginPx = margin
        )

        assertNotNull(ltrBounds)
        assertEquals(cardBounds.right - markerSizePx - margin, ltrBounds!!.left)
        assertEquals(cardBounds.right - margin, ltrBounds.right)
    }

    @Test
    fun testCardPartiallyOrFullyOutsideScreenRejected() {
        val markerSizePx = 64

        // Card sticking out top of screen
        val cardAboveScreen = Rect(100, -50, 980, 300)
        val resultTop = OverlayPositionHelper.calculateMarkerBounds(
            cardBounds = cardAboveScreen,
            markerSizePx = markerSizePx,
            isRtl = true,
            screenBounds = screenBounds
        )
        assertNull("Partially off-screen card at top must return null", resultTop)

        // Card sticking out bottom of screen
        val cardBelowScreen = Rect(100, 2200, 980, 2500)
        val resultBottom = OverlayPositionHelper.calculateMarkerBounds(
            cardBounds = cardBelowScreen,
            markerSizePx = markerSizePx,
            isRtl = true,
            screenBounds = screenBounds
        )
        assertNull("Partially off-screen card at bottom must return null", resultBottom)
    }

    @Test
    fun testStaleMarkRemoval() {
        val seenTime = 10_000L

        // After 200 ms: not stale
        assertFalse(OverlayPositionHelper.isStale(seenTime, seenTime + 200L, staleThresholdMs = 400L))

        // Exactly 400 ms: not stale
        assertFalse(OverlayPositionHelper.isStale(seenTime, seenTime + 400L, staleThresholdMs = 400L))

        // After 401 ms: stale!
        assertTrue(OverlayPositionHelper.isStale(seenTime, seenTime + 401L, staleThresholdMs = 400L))
        assertTrue(OverlayPositionHelper.isStale(seenTime, seenTime + 1000L, staleThresholdMs = 400L))
    }
}
