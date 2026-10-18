package com.driveflow.driver.license

import com.driveflow.driver.core.di.AppGraph
import com.driveflow.driver.core.di.LicenseVerificationResult
import com.driveflow.driver.core.model.LicenseStatus

interface LicenseRepository {
    suspend fun activate(token: String): LicenseVerificationResult
    suspend fun getRemainingDays(): Int
}

class DefaultLicenseRepository : LicenseRepository {
    override suspend fun activate(token: String): LicenseVerificationResult {
        val trimmed = token.trim()
        val verifier = AppGraph.licenseVerifier
        val currentDay = AppGraph.clock.currentUtcDay()
        val clockHighWater = AppGraph.settingsStore.getClockHighWaterDay()
        val deviceId = DeviceHelper.normalizeDeviceId(AppGraph.settingsStore.getContactPhone())
        val result = verifier.verify(
            token = trimmed,
            publicKeySpkiB64 = AppGraph.licensePublicKey,
            deviceIdHex = deviceId,
            currentDay = currentDay,
            clockHighWaterDay = clockHighWater
        )
        if (result is LicenseVerificationResult.Valid) {
            AppGraph.settingsStore.setLicenseToken(trimmed)
            AppGraph.settingsStore.updateClockHighWaterDayIfGreater(currentDay)
        }
        return result
    }

    override suspend fun getRemainingDays(): Int {
        val token = AppGraph.settingsStore.getLicenseToken()
        if (token.isNullOrBlank()) return 0
        return 30
    }
}
