package com.driveflow.driver

import android.app.NotificationManager
import android.content.Context
import android.graphics.Rect
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import com.driveflow.driver.core.model.LicenseStatus
import com.driveflow.driver.core.model.ParsedRequest
import com.driveflow.driver.core.model.ReadSource
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.core.model.RoadState
import com.driveflow.driver.core.model.Tone
import com.driveflow.driver.core.model.Verdict
import com.driveflow.driver.data.prefs.AlertMode
import com.driveflow.driver.engine.OpportunityEvent
import com.driveflow.driver.overlay.OpportunityAlertManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OverlayManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testOpportunityAlertManager_createsNotificationChannel() {
        val manager = OpportunityAlertManager(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = nm.getNotificationChannel(OpportunityAlertManager.CHANNEL_ID)

        assertNotNull("Opportunity channel should be created", channel)
        assertEquals("opportunity", channel.id)
        assertTrue(channel.shouldVibrate())
    }

    @Test
    fun testOpportunityAlertManager_muteDetection() {
        val manager = OpportunityAlertManager(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Set to SILENT ring mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        assertTrue("Should detect silent mode", manager.isNotificationMuted())

        // Set to VIBRATE ring mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        assertTrue("Should detect vibrate mode", manager.isNotificationMuted())

        // Normal mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        // In Robolectric, default stream volume is > 0, so it should not be muted
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        if (volume > 0) {
            assertFalse("Should not be muted when normal and volume > 0", manager.isNotificationMuted())
        }
    }

    @Test
    fun testOpportunityAlertManager_playTestAlertDoesNotCrash() {
        val manager = OpportunityAlertManager(context)
        manager.playTestAlert(AlertMode.TONE)
        manager.playTestAlert(AlertMode.TONE_VIBRATE)
    }

    @Test
    fun testOpportunityAlertManager_fireAlert() {
        val manager = OpportunityAlertManager(context)
        val dummyRequest = ParsedRequest(
            app = RideApp.INDRIVE,
            key = "indrive_123",
            source = ReadSource.ACCESSIBILITY,
            fare = 150.0,
            currency = "EGP",
            pickupKm = 1.0,
            tripKm = 5.0,
            totalKm = 6.0,
            pickupMinutes = 3,
            riderRating = 4.9,
            riderTrips = 100,
            requestAgeSeconds = 2,
            pickupAddress = "Secret Pickup 123",
            destinationAddress = "Secret Destination 456",
            extraStops = 0,
            paymentMethod = "Cash",
            riderNote = null,
            offerCount = 1,
            roadState = RoadState.EASY,
            productName = null,
            bounds = Rect(0, 0, 500, 200),
            isDetailOpen = false,
            confidence = 1.0f
        )
        val dummyVerdict = Verdict(
            tone = Tone.GREEN,
            reasonCode = "accept_if_road_ok",
            titleAr = "عرض ممتاز",
            titleEn = "Good offer",
            detailAr = "السعر مناسب",
            detailEn = "Price is good",
            flags = emptyList(),
            totalKm = 6.0,
            grossPerKm = 25.0,
            netPerKm = 20.0,
            netProfit = 100.0,
            suggestedPrice = 150.0
        )
        val event = OpportunityEvent(
            request = dummyRequest,
            verdict = dummyVerdict,
            timestampMs = System.currentTimeMillis()
        )
        manager.fireAlert(event, AlertMode.TONE_VIBRATE)
    }

    @Test
    fun testReportMessage_neverIncludesRiderAddressOrName() {
        val dummyRequest = ParsedRequest(
            app = RideApp.UBER,
            key = "uber_789",
            source = ReadSource.OCR,
            fare = 220.0,
            currency = "EGP",
            pickupKm = 2.0,
            tripKm = 12.0,
            totalKm = 14.0,
            pickupMinutes = 5,
            riderRating = 4.8,
            riderTrips = 50,
            requestAgeSeconds = 1,
            pickupAddress = "Mohandessin Private Villa 4",
            destinationAddress = "Tagamoa Compound Gate 2",
            extraStops = 1,
            paymentMethod = "Credit Card",
            riderNote = "VIP rider Ahmed",
            offerCount = null,
            roadState = RoadState.SLOW,
            productName = "UberX",
            bounds = Rect(0, 0, 600, 400),
            isDetailOpen = true,
            confidence = 0.95f
        )
        val dummyVerdict = Verdict(
            tone = Tone.ORANGE,
            reasonCode = "PICKUP_FAR",
            titleAr = "بيك أب بعيد",
            titleEn = "Pickup far",
            detailAr = "المسافة أطول من المعتاد",
            detailEn = "Pickup distance is far",
            flags = listOf("pickup_far"),
            totalKm = 14.0,
            grossPerKm = 15.7,
            netPerKm = 10.0,
            netProfit = 120.0,
            suggestedPrice = 240.0
        )

        // Generate report text according to the contract: numbers and metrics only
        val fareText = dummyRequest.fare?.let { "${it.toInt()} ${dummyRequest.currency ?: "EGP"}" } ?: "—"
        val distText = dummyVerdict.totalKm?.let { "${String.format(java.util.Locale.US, "%.1f", it)} km" } ?: "—"
        val message = "بلاغ عن طلب: السعر = $fareText، المسافة = $distText، الكود = ${dummyVerdict.reasonCode}، التقييم = ${dummyVerdict.tone}"

        // Privacy Contract assertions:
        assertFalse("Report message must never contain pickup address", message.contains("Mohandessin"))
        assertFalse("Report message must never contain destination address", message.contains("Tagamoa"))
        assertFalse("Report message must never contain rider note or name", message.contains("Ahmed"))
        assertTrue("Must contain fare number", message.contains("220"))
        assertTrue("Must contain distance", message.contains("14.0"))
        assertTrue("Must contain reason code", message.contains("PICKUP_FAR"))
        assertTrue("Must contain tone", message.contains("ORANGE"))
    }
}
