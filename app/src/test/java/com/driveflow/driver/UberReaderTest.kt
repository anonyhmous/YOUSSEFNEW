package com.driveflow.driver

import android.graphics.Rect
import com.driveflow.driver.core.model.ReadSource
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.ocr.OcrLine
import com.driveflow.driver.ocr.OcrResult
import com.driveflow.driver.reader.uber.UberReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UberReaderTest {
    private val reader = UberReader()

    @Test
    fun testParseEnglishUberRequest() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("UberX", Rect(50, 1000, 250, 1050), 0.95f),
                OcrLine("145.00 EGP", Rect(50, 1070, 350, 1150), 0.98f),
                OcrLine("6 min (2.0 km) away", Rect(50, 1160, 500, 1210), 0.92f),
                OcrLine("18 min (11.5 km) trip", Rect(50, 1220, 550, 1270), 0.94f),
                OcrLine("★ 4.92 • (250)", Rect(700, 1000, 950, 1050), 0.90f),
                OcrLine("Cairo Festival City", Rect(50, 1300, 600, 1350), 0.88f),
                OcrLine("Nasr City", Rect(50, 1370, 600, 1420), 0.88f)
            )
        )

        val parsed = reader.parseOcrResult(ocrResult)
        assertNotNull(parsed)
        assertEquals(RideApp.UBER, parsed!!.app)
        assertEquals(ReadSource.OCR, parsed.source)
        assertEquals(145.0, parsed.fare!!, 0.001)
        assertEquals("EGP", parsed.currency)
        assertEquals(6, parsed.pickupMinutes)
        assertEquals(2.0, parsed.pickupKm!!, 0.01)
        assertEquals(11.5, parsed.tripKm!!, 0.01)
        assertEquals(13.5, parsed.totalKm!!, 0.01)
        assertEquals(4.92, parsed.riderRating!!, 0.01)
        assertEquals("UberX", parsed.productName)
        assertEquals("Cairo Festival City", parsed.pickupAddress)
        assertEquals("Nasr City", parsed.destinationAddress)
        assertEquals(0, parsed.extraStops)
        assertTrue(parsed.confidence >= 0.85f)
    }

    @Test
    fun testParseArabicUberSaverWithStops() {
        val ocrResult = OcrResult(
            lines = listOf(
                OcrLine("Uber Saver", Rect(50, 950, 300, 1000), 0.92f),
                OcrLine("95 جنيه", Rect(50, 1020, 250, 1080), 0.96f),
                OcrLine("4 دقيقة (1.5 كم)", Rect(50, 1100, 350, 1150), 0.90f),
                OcrLine("12 دقيقة (5.0 كم)", Rect(50, 1170, 450, 1220), 0.91f),
                OcrLine("4.85 ★", Rect(750, 950, 900, 1000), 0.89f),
                OcrLine("2 وقفات", Rect(50, 1240, 200, 1280), 0.85f),
                OcrLine("المهندسين", Rect(50, 1300, 500, 1340), 0.85f),
                OcrLine("الدقي", Rect(50, 1360, 500, 1400), 0.85f)
            )
        )

        val parsed = reader.parseOcrResult(ocrResult)
        assertNotNull(parsed)
        assertEquals(95.0, parsed!!.fare!!, 0.001)
        assertEquals(1.5, parsed.pickupKm!!, 0.01)
        assertEquals(5.0, parsed.tripKm!!, 0.01)
        assertEquals(6.5, parsed.totalKm!!, 0.01)
        assertEquals(4.85, parsed.riderRating!!, 0.01)
        assertTrue(parsed.productName!!.contains("Saver", ignoreCase = true))
        assertEquals(1, parsed.extraStops)
    }

    @Test
    fun testNonRideScreenReturnsNull() {
        val emptyResult = OcrResult(emptyList())
        assertNull(reader.parseOcrResult(emptyResult))

        val randomScreen = OcrResult(
            lines = listOf(
                OcrLine("Settings", Rect(10, 10, 100, 50), 0.9f),
                OcrLine("Wi-Fi & Network", Rect(10, 60, 200, 100), 0.9f)
            )
        )
        assertNull(reader.parseOcrResult(randomScreen))
    }
}
