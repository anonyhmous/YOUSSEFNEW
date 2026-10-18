package com.driveflow.driver

import android.graphics.Rect
import com.driveflow.driver.ocr.OcrLine
import com.driveflow.driver.ocr.OcrResult
import com.driveflow.driver.reader.indrive.InDriveTripDistanceOcr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InDriveTripDistanceOcrTest {
    private val extractor = InDriveTripDistanceOcr()
    private val screenBounds = Rect(0, 0, 1080, 2400)
    private val mapBounds = Rect(0, 0, 1080, 1200)

    @Test
    fun testValidEnglishTripDistance() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("6.4 km", Rect(450, 600, 630, 660), 0.95f)
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertNull(result.failure)
        assertNotNull(result.value)
        assertEquals(6.4, result.value!!, 0.01)
    }

    @Test
    fun testValidArabicTripDistance() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("١٢٫٥ كم", Rect(450, 500, 650, 560), 0.92f)
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertNull(result.failure)
        assertNotNull(result.value)
        assertEquals(12.5, result.value!!, 0.01)
    }

    @Test
    fun testTripBoxMissing() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("Cairo Route", Rect(100, 200, 300, 250), 0.90f)
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertEquals("trip_box_missing", result.failure)
        assertNull(result.value)
    }

    @Test
    fun testDigitsUnclearWhenLowConfidence() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("8.5 km", Rect(400, 400, 600, 450), 0.45f) // confidence < 0.60
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertEquals("digits_unclear", result.failure)
        assertNull(result.value)
    }

    @Test
    fun testTripKmClippedAtScreenEdge() {
        val ocrResult = OcrResult(
            lines = listOf(
                // Box touches left screen edge (x = 4 <= 8)
                OcrLine("7.2 km", Rect(4, 500, 200, 560), 0.92f)
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertEquals("trip_km_clipped", result.failure)
        assertNull(result.value)
    }

    @Test
    fun testTripKmCoveredAtMapBoundary() {
        val ocrResult = OcrResult(
            lines = listOf(
                // Box touches bottom of map boundary (y = 1198 >= 1200 - 4)
                OcrLine("5.0 km", Rect(400, 1150, 600, 1198), 0.90f)
            )
        )
        val result = extractor.parseOcrResult(ocrResult, mapBounds, screenBounds)
        assertEquals("trip_km_covered", result.failure)
        assertNull(result.value)
    }
}
