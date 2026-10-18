package com.driveflow.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.driveflow.driver.core.di.AppGraph
import com.driveflow.driver.core.util.LocaleHelper
import com.driveflow.driver.ui.navigation.AppNavGraph
import com.driveflow.driver.ui.theme.BrandNavyBackground
import com.driveflow.driver.ui.theme.DriveFlowTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            val settingsStore = AppGraph.settingsStore
            val currentLanguage by settingsStore.languageFlow.collectAsState(initial = "ar")
            val isRtl = LocaleHelper.isRtl(currentLanguage)

            DriveFlowTheme(isRtl = isRtl) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BrandNavyBackground),
                    color = BrandNavyBackground
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
