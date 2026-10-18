package com.driveflow.driver.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveflow.driver.R
import com.driveflow.driver.core.model.LicenseStatus
import com.driveflow.driver.core.model.Policy
import com.driveflow.driver.core.model.RideApp
import com.driveflow.driver.data.prefs.AlertMode
import com.driveflow.driver.ui.theme.BrandGold
import com.driveflow.driver.ui.theme.BrandMint
import com.driveflow.driver.ui.theme.BrandNavyBackground
import com.driveflow.driver.ui.theme.BrandNavyBorder
import com.driveflow.driver.ui.theme.BrandNavySurface
import com.driveflow.driver.ui.theme.BrandNavySurfaceVariant
import com.driveflow.driver.ui.theme.DriveFlowTheme
import com.driveflow.driver.ui.theme.TextMuted
import com.driveflow.driver.ui.theme.TextWhite
import com.driveflow.driver.ui.theme.VerdictGreen
import com.driveflow.driver.ui.theme.VerdictOrange

@Composable
fun HomeScreen(
    policy: Policy,
    alertEnabled: Boolean,
    alertMode: AlertMode,
    headingHomeEnabled: Boolean,
    licenseStatus: LicenseStatus,
    remainingDays: Int,
    freeListRemaining: Int,
    freeDetailsRemaining: Int,
    readerEnabled: Boolean,
    onToggleReader: (Boolean) -> Unit,
    onToggleAlert: (Boolean) -> Unit,
    onToggleHeadingHome: (Boolean) -> Unit,
    onToggleAppEnabled: (RideApp, Boolean) -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToHeadingHome: () -> Unit,
    onNavigateToSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRestrictedDialog by remember { mutableStateOf(false) }
    var showBatteryGuideDialog by remember { mutableStateOf(false) }

    var isAccessibilityGranted by remember { mutableStateOf(readerEnabled) }
    var isOverlayGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
        )
    }

    val isRulesConfigured = policy.minPricePerKm > 0.0
    val isSetupComplete = isAccessibilityGranted && isOverlayGranted && isRulesConfigured

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyBackground)
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
            .testTag("home_screen")
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_driveflow_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandMint
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Card: DriveFlow is ON / OFF
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (readerEnabled) BrandNavySurface else BrandNavySurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (readerEnabled) BrandMint.copy(alpha = 0.6f) else BrandNavyBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (readerEnabled) BrandMint.copy(alpha = 0.2f) else BrandNavyBorder.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (readerEnabled) BrandMint else TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (readerEnabled) stringResource(R.string.home_hero_on) else stringResource(R.string.home_hero_off),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (readerEnabled) stringResource(R.string.home_hero_sub_on) else stringResource(R.string.home_hero_sub_off),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = readerEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isAccessibilityGranted) {
                            showRestrictedDialog = true
                        } else {
                            onToggleReader(isChecked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandNavyBackground,
                        checkedTrackColor = BrandMint,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BrandNavySurfaceVariant
                    ),
                    modifier = Modifier.testTag("reader_main_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-Step Setup Checklist (if not yet complete)
        if (!isSetupComplete) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.setup_checklist_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step 1: Accessibility
                    SetupStepRow(
                        title = stringResource(R.string.setup_step1),
                        isDone = isAccessibilityGranted,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                showRestrictedDialog = true
                            } else {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2: Overlay
                    SetupStepRow(
                        title = stringResource(R.string.setup_step2),
                        isDone = isOverlayGranted,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 3: Rules
                    SetupStepRow(
                        title = stringResource(R.string.setup_step3),
                        isDone = isRulesConfigured,
                        onClick = onNavigateToRules
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Control Rows
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Row: My Rules
                val rulesSummary = if (policy.minPricePerKm > 0.0) {
                    "الحد الأدنى: ${policy.minPricePerKm} ${policy.currency}/كم"
                } else {
                    "غير محدد بعد"
                }

                HomeFeatureRow(
                    icon = Icons.Default.Rule,
                    title = stringResource(R.string.row_my_rules),
                    subtitle = rulesSummary,
                    onClick = onNavigateToRules,
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BrandNavyBorder)
                )

                // Row: Alert
                val alertSub = if (alertEnabled) {
                    if (alertMode == AlertMode.TONE_VIBRATE) "صوت واهتزاز" else "صوت فقط"
                } else {
                    "مكتوم"
                }

                HomeFeatureRow(
                    icon = Icons.Default.NotificationsActive,
                    title = stringResource(R.string.row_alerts),
                    subtitle = alertSub,
                    onClick = onNavigateToAlerts,
                    trailingContent = {
                        Switch(
                            checked = alertEnabled,
                            onCheckedChange = onToggleAlert,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandNavyBackground,
                                checkedTrackColor = BrandGold
                            )
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BrandNavyBorder)
                )

                // Row: Heading Home
                HomeFeatureRow(
                    icon = Icons.Default.Home,
                    title = stringResource(R.string.row_heading_home),
                    subtitle = if (headingHomeEnabled) "مفعل (يركز على وجهتك)" else "متوقف",
                    onClick = onNavigateToHeadingHome,
                    trailingContent = {
                        Switch(
                            checked = headingHomeEnabled,
                            onCheckedChange = onToggleHeadingHome,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandNavyBackground,
                                checkedTrackColor = BrandMint
                            )
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BrandNavyBorder)
                )

                // Row: Plan & License
                val statusColor = when (licenseStatus) {
                    LicenseStatus.ACTIVE -> VerdictGreen
                    LicenseStatus.TRIAL -> VerdictOrange
                    LicenseStatus.FREE -> BrandMint
                }
                val statusText = when (licenseStatus) {
                    LicenseStatus.ACTIVE -> "اشتراك نشط ($remainingDays يوم)"
                    LicenseStatus.TRIAL -> "فترة تجريبية ($remainingDays يوم)"
                    LicenseStatus.FREE -> "مجاني (قائمة: $freeListRemaining | تفاصيل: $freeDetailsRemaining)"
                }

                HomeFeatureRow(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.row_plan),
                    subtitle = statusText,
                    onClick = onNavigateToSubscribe,
                    trailingContent = {
                        Button(
                            onClick = onNavigateToSubscribe,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = statusColor.copy(alpha = 0.2f),
                                contentColor = statusColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stringResource(R.string.btn_details),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Supported Apps Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.supported_apps_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                RideApp.entries.forEach { app ->
                    val isAppEnabled = policy.appEnabled[app] ?: true
                    val appName = when (app) {
                        RideApp.INDRIVE -> stringResource(R.string.app_indrive)
                        RideApp.UBER -> stringResource(R.string.app_uber)
                        RideApp.DIDI -> stringResource(R.string.app_didi)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = appName, color = TextWhite, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isAppEnabled,
                            onCheckedChange = { onToggleAppEnabled(app, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandNavyBackground,
                                checkedTrackColor = BrandMint
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keep Reader Running (Battery optimization guide)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBatteryGuideDialog = true },
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BatterySaver,
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.keep_reader_running_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.keep_reader_running_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Android 13+ Restricted Settings Dialog
    if (showRestrictedDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictedDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.restricted_settings_title),
                    color = BrandGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.restricted_settings_msg),
                    color = TextWhite,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestrictedDialog = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = BrandNavyBackground)
                ) {
                    Text(stringResource(R.string.btn_understood), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestrictedDialog = false }) {
                    Text(stringResource(R.string.btn_skip), color = TextMuted)
                }
            },
            containerColor = BrandNavySurface
        )
    }

    // Battery Optimization Guide Dialog
    if (showBatteryGuideDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryGuideDialog = false },
            title = {
                Text(
                    text = "دليل إبقاء التطبيق يعمل بالخلفية",
                    color = BrandGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "لضمان استمرار قراءة الشاشة أثناء فتح تطبيقات المشاوير:\n\n" +
                                "١. هواتف شاومي (Xiaomi / Redmi / Poco):\n" +
                                "فعل التشغيل التلقائي (Autostart) وألغِ توفير البطارية.\n\n" +
                                "٢. هواتف أوبو وريلمي (Oppo / Realme):\n" +
                                "اسمح بالنشاط في الخلفية وثبت التطبيق في قائمة التطبيقات المفتوحة.\n\n" +
                                "٣. هواتف سامسونج (Samsung):\n" +
                                "استثنِ التطبيق من وضع السكون العميق وضع بطارية غير مقيد لـ DriveFlow.",
                        color = TextWhite,
                        lineHeight = 22.sp,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatteryGuideDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandMint, contentColor = BrandNavyBackground)
                ) {
                    Text("فتح إعدادات البطارية", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryGuideDialog = false }) {
                    Text("إغلاق", color = TextMuted)
                }
            },
            containerColor = BrandNavySurface
        )
    }
}

@Composable
fun SetupStepRow(
    title: String,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) BrandMint else VerdictOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = if (isDone) TextMuted else TextWhite,
                fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun HomeFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
        trailingContent()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DriveFlowTheme {
        HomeScreen(
            policy = Policy(minPricePerKm = 8.5),
            alertEnabled = true,
            alertMode = AlertMode.TONE_VIBRATE,
            headingHomeEnabled = false,
            licenseStatus = LicenseStatus.FREE,
            remainingDays = 0,
            freeListRemaining = 18,
            freeDetailsRemaining = 4,
            readerEnabled = true,
            onToggleReader = {},
            onToggleAlert = {},
            onToggleHeadingHome = {},
            onToggleAppEnabled = { _, _ -> },
            onNavigateToRules = {},
            onNavigateToAlerts = {},
            onNavigateToHeadingHome = {},
            onNavigateToSubscribe = {}
        )
    }
}
