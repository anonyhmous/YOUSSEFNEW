package com.driveflow.driver

import com.driveflow.driver.ocr.OcrThrottleController
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrThrottleControllerTest {
    @Test
    fun testRateLimiting600ms() {
        val controller = OcrThrottleController(minIntervalMs = 600L)

        // Initial cycle permitted
        assertTrue(controller.shouldProcess(currentTimeMs = 1000L))
        controller.recordOcrProcessed(currentTimeMs = 1000L)

        // At 1300ms (300ms later), should be blocked (< 600ms)
        assertFalse(controller.shouldProcess(currentTimeMs = 1300L))

        // At 1599ms (599ms later), should still be blocked
        assertFalse(controller.shouldProcess(currentTimeMs = 1599L))

        // At 1600ms (600ms later), should be permitted
        assertTrue(controller.shouldProcess(currentTimeMs = 1600L))
    }

    @Test
    fun testScreenOffBlocksProcessing() {
        val controller = OcrThrottleController(minIntervalMs = 600L)
        controller.setScreenStateForTesting(false)

        assertFalse(controller.shouldProcess(currentTimeMs = 5000L))

        controller.setScreenStateForTesting(true)
        assertTrue(controller.shouldProcess(currentTimeMs = 5000L))
    }

    @Test
    fun testContentHashSkipping() {
        val controller = OcrThrottleController()

        // First hash: content changed
        assertTrue(controller.checkAndRecordContentChanged(123456789L))

        // Same hash: unchanged, skip OCR
        assertFalse(controller.checkAndRecordContentChanged(123456789L))

        // New hash: changed!
        assertTrue(controller.checkAndRecordContentChanged(987654321L))
    }
}
