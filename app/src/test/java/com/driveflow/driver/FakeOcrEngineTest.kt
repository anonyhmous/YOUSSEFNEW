package com.driveflow.driver

import android.graphics.Rect
import android.graphics.RectF
import com.driveflow.driver.ocr.FakeOcrEngine
import com.driveflow.driver.ocr.OcrLine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FakeOcrEngineTest {
    @Test
    fun testCannedLines() = runBlocking {
        val fakeEngine = FakeOcrEngine()
        fakeEngine.setCannedText(listOf("UberX", "150 EGP"), confidence = 0.95f)

        val result = fakeEngine.read(null, RectF(0f, 0f, 1f, 1f))
        assertEquals(2, result.lines.size)
        assertEquals("UberX", result.lines[0].text)
        assertEquals("150 EGP", result.lines[1].text)
        assertEquals(0.95f, result.lines[0].confidence, 0.001f)
    }

    @Test
    fun testRegionSpecificCannedLines() = runBlocking {
        val fakeEngine = FakeOcrEngine()
        fakeEngine.setRegionCanned(
            "0.5",
            listOf(OcrLine("12.5 km", Rect(10, 10, 50, 50), 0.98f))
        )

        val result = fakeEngine.read(null, RectF(0.5f, 0.5f, 1f, 1f))
        assertEquals(1, result.lines.size)
        assertEquals("12.5 km", result.lines[0].text)
    }
}
