package com.driveflow.driver

import android.graphics.Rect
import com.driveflow.driver.core.model.ReadSource
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.ocr.OcrLine
import com.driveflow.driver.ocr.OcrResult
import com.driveflow.driver.reader.didi.DidiReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DidiReaderTest {
    private val reader = DidiReader()

    @Test
    fun testParseDidiWithConsistentRate() {
        // Fare 100 EGP, 10 km total (1.0 pickup + 9.0 trip), printed rate 10.0 EGP/km
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("100.00 EGP", Rect(50, 900, 300, 960), 0.95f),
                OcrLine("10.00 EGP/km", Rect(50, 970, 300, 1020), 0.95f),
                OcrLine("3 min (1.0 km) away", Rect(50, 1030, 450, 1080), 0.90f),
                OcrLine("15 min (9.0 km) trip", Rect(50, 1090, 450, 1140), 0.90f),
                OcrLine("★ 4.8", Rect(700, 900, 850, 960), 0.90f),
                OcrLine("Tagamoa", Rect(50, 1160, 400, 1200), 0.85f),
                OcrLine("Maadi", Rect(50, 1220, 400, 1260), 0.85f)
            )
        )

        val parsed = reader.parseOcrResult(ocrResult)
        assertNotNull(parsed)
        assertEquals(RideApp.DIDI, parsed!!.app)
        assertEquals(ReadSource.OCR, parsed.source)
        assertEquals(100.0, parsed.fare!!, 0.001)
        assertEquals(1.0, parsed.pickupKm!!, 0.01)
        assertEquals(9.0, parsed.tripKm!!, 0.01)
        assertEquals(10.0, parsed.totalKm!!, 0.01)
        assertEquals(4.8, parsed.riderRating!!, 0.01)
        // Confidence should remain high because 100 / 10 = 10, perfectly matches printed rate 10.0
        assertTrue(parsed.confidence >= 0.85f)
    }

    @Test
    fun testParseDidiWithDiscrepantRateReducesConfidence() {
        // Fare 60 EGP, 10 km total -> computed rate = 6.0 EGP/km
        // Printed rate says 12.0 EGP/km (100% discrepancy!)
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("60.00 EGP", Rect(50, 900, 300, 960), 0.95f),
                OcrLine("12.00 EGP/km", Rect(50, 970, 300, 1020), 0.95f),
                OcrLine("3 min (2.0 km)", Rect(50, 1030, 450, 1080), 0.90f),
                OcrLine("15 min (8.0 km)", Rect(50, 1090, 450, 1140), 0.90f)
            )
        )

        val parsed = reader.parseOcrResult(ocrResult)
        assertNotNull(parsed)
        assertEquals(60.0, parsed!!.fare!!, 0.001)
        assertEquals(10.0, parsed.totalKm!!, 0.01)
        // Confidence should be heavily penalized (< 0.70) due to >20% mismatch
        assertTrue("Confidence was ${parsed.confidence}, expected penalized < 0.70", parsed.confidence < 0.70f)
    }

    @Test
    fun testParseArabicDidi() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("75 جنيه", Rect(50, 850, 200, 900), 0.95f),
                OcrLine("12.5 جنيه/كم", Rect(50, 910, 250, 950), 0.92f),
                OcrLine("5 دقيقة (2.0 كم)", Rect(50, 960, 300, 1000), 0.90f),
                OcrLine("10 دقيقة (4.0 كم)", Rect(50, 1010, 300, 1050), 0.90f),
                OcrLine("★ 4.9", Rect(700, 850, 850, 900), 0.92f)
            )
        )

        val parsed = reader.parseOcrResult(ocrResult)
        assertNotNull(parsed)
        assertEquals(75.0, parsed!!.fare!!, 0.001)
        assertEquals(2.0, parsed.pickupKm!!, 0.01)
        assertEquals(4.0, parsed.tripKm!!, 0.01)
        assertEquals(6.0, parsed.totalKm!!, 0.01)
        assertEquals(4.9, parsed.riderRating!!, 0.01)
    }
}
