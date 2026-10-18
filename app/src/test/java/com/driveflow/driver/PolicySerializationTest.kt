package com.driveflow.driver

import com.driveflow.driver.core.model.DistanceUnit
import com.driveflow.driver.core.model.LongTripMode
import com.driveflow.driver.core.model.Policy
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.core.model.StopsMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicySerializationTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun defaultPolicy_serializesAndDeserializesCorrectly() {
        val original = Policy()
        val jsonString = json.encodeToString(original)

        assertNotNull(jsonString)
        assertTrue(jsonString.contains("\"currency\": \"EGP\""))
        assertTrue(jsonString.contains("\"negotiationStep\": 5"))

        val deserialized = json.decodeFromString<Policy>(jsonString)
        assertEquals(original, deserialized)
        assertEquals(0.0, deserialized.carCostPerKm, 0.001)
        assertEquals(DistanceUnit.KM, deserialized.distanceUnit)
        assertEquals(StopsMode.IGNORE, deserialized.stopsMode)
        assertEquals(LongTripMode.IGNORE, deserialized.longTripMode)
    }

    @Test
    fun customizedPolicy_serializesAndDeserializesAllFields() {
        val original = Policy(
            minPricePerKm = 12.5,
            minTotalKm = 2.0,
            maxTotalKm = 45.0,
            maxTripKm = 35.0,
            maxPickupKm = 4.5,
            maxPickupMinutes = 8,
            minRiderRating = 4.7,
            minRiderTrips = 15,
            maxRequestAgeMinutes = 3,
            negotiationStep = 10,
            maxOffer = 500.0,
            currency = "EGP",
            governorate = "Cairo",
            avoidedAreas = listOf("Ring Road Stop", "Narrow Alley A"),
            preferredAreas = listOf("Tagamoa", "Maadi", "Zamalek"),
            allowedPayments = setOf("CASH", "CARD"),
            bannedNoteWords = listOf("smoke", "pet", "wait"),
            stopsMode = StopsMode.WARN,
            stopFee = 25.0,
            longTripMode = LongTripMode.WARN,
            longTripKm = 40.0,
            warnOnUberSaver = true,
            freePickupKm = 1.0,
            fuelCostPerKm = 2.25,
            upkeepCostPerKm = 1.15,
            commissionPct = mapOf(
                RideApp.INDRIVE to 10.0,
                RideApp.UBER to 22.5,
                RideApp.DIDI to 18.0
            ),
            commissionEnabled = mapOf(
                RideApp.INDRIVE to true,
                RideApp.UBER to true,
                RideApp.DIDI to false
            ),
            appEnabled = mapOf(
                RideApp.INDRIVE to true,
                RideApp.UBER to true,
                RideApp.DIDI to false
            ),
            distanceUnit = DistanceUnit.KM,
            basisNet = true
        )

        val jsonString = json.encodeToString(original)
        val deserialized = json.decodeFromString<Policy>(jsonString)

        assertEquals(original, deserialized)
        assertEquals(12.5, deserialized.minPricePerKm, 0.001)
        assertEquals(3.40, deserialized.carCostPerKm, 0.001) // 2.25 + 1.15
        assertEquals(StopsMode.WARN, deserialized.stopsMode)
        assertEquals(LongTripMode.WARN, deserialized.longTripMode)
        assertEquals(true, deserialized.basisNet)
        assertEquals(listOf("Tagamoa", "Maadi", "Zamalek"), deserialized.preferredAreas)
        assertEquals(10.0, deserialized.commissionPct[RideApp.INDRIVE])
        assertEquals(false, deserialized.appEnabled[RideApp.DIDI])
    }

    @Test
    fun policy_handlesLenientJsonWithMissingOrExtraFields() {
        val rawJson = """
            {
                "minPricePerKm": 8.0,
                "unknownFieldShouldBeIgnored": "abc",
                "currency": "EGP"
            }
        """.trimIndent()

        val deserialized = json.decodeFromString<Policy>(rawJson)
        assertEquals(8.0, deserialized.minPricePerKm, 0.001)
        assertEquals("EGP", deserialized.currency)
        assertEquals(5, deserialized.negotiationStep)
        assertEquals(StopsMode.IGNORE, deserialized.stopsMode)
    }
}
