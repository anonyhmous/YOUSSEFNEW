package com.driveflow.driver.ui.screens.settings

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveflow.driver.R
import com.driveflow.driver.data.prefs.BarPosition
import com.driveflow.driver.data.prefs.MarkerSize
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
import com.driveflow.driver.ui.theme.VerdictRed

@Composable
fun SettingsScreen(
    currentMarkerSize: MarkerSize,
    onSelectMarkerSize: (MarkerSize) -> Unit,
    barOpacityPercent: Int,
    onChangeBarOpacity: (Int) -> Unit,
    barPosition: BarPosition,
    onResetBarPosition: () -> Unit,
    autoOffHours: Int,
    onSelectAutoOffHours: (Int) -> Unit,
    currentLanguage: String,
    onToggleLanguage: () -> Unit,
    onNavigateToSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyBackground)
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Text(
            text = stringResource(R.string.settings_title_header),
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "تخصيص المظهر والشريط العائم والأداء",
            style = MaterialTheme.typography.bodySmall,
            color = BrandMint
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Marker Size + Live Sample List
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = BrandGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.marker_size_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MarkerSize.entries.forEach { size ->
                        val label = when (size) {
                            MarkerSize.SMALL -> stringResource(R.string.marker_size_small)
                            MarkerSize.NORMAL -> stringResource(R.string.marker_size_normal)
                            MarkerSize.LARGE -> stringResource(R.string.marker_size_large)
                        }
                        FilterChip(
                            selected = currentMarkerSize == size,
                            onClick = { onSelectMarkerSize(size) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGold,
                                selectedLabelColor = BrandNavyBackground
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview: 3 Cards (Green, Orange, Red)
                Text(
                    text = stringResource(R.string.marker_preview_title),
                    color = BrandGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val badgePadding = when (currentMarkerSize) {
                    MarkerSize.SMALL -> 4.dp
                    MarkerSize.NORMAL -> 8.dp
                    MarkerSize.LARGE -> 12.dp
                }
                val textSize = when (currentMarkerSize) {
                    MarkerSize.SMALL -> 11.sp
                    MarkerSize.NORMAL -> 13.sp
                    MarkerSize.LARGE -> 15.sp
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SampleRequestCard("inDrive • 120 ج (9 كم)", "13.3 ج/كم", VerdictGreen, badgePadding, textSize)
                    SampleRequestCard("Uber • 65 ج (7 كم)", "9.2 ج/كم", VerdictOrange, badgePadding, textSize)
                    SampleRequestCard("DiDi • 40 ج (8 كم)", "5.0 ج/كم", VerdictRed, badgePadding, textSize)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Bar Opacity Slider + Live Sample Bar
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Opacity, contentDescription = null, tint = BrandMint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.bar_opacity_label, barOpacityPercent),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = barOpacityPercent.toFloat(),
                    onValueChange = { onChangeBarOpacity(it.toInt()) },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandMint,
                        activeTrackColor = BrandMint,
                        inactiveTrackColor = BrandNavyBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Sample Bar Preview
                Text(
                    text = stringResource(R.string.bar_preview_title),
                    color = BrandMint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val sampleAlpha = barOpacityPercent / 100f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandNavySurfaceVariant.copy(alpha = sampleAlpha))
                        .border(1.dp, BrandGold.copy(alpha = sampleAlpha), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(VerdictGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("135 ج (11 كم)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("صافي: +92 ج", color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Bar Position (Auto vs Custom with reset)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.bar_pos_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (barPosition.isAuto) stringResource(R.string.bar_pos_auto) else stringResource(R.string.bar_pos_custom),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                IconButton(
                    onClick = onResetBarPosition,
                    modifier = Modifier.testTag("reset_bar_position_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.btn_reset_pos),
                        tint = BrandGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Auto-Off Timer with Bank App note
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = BrandGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.auto_off_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 3, 6, 0).forEach { hours ->
                        val label = when (hours) {
                            1 -> stringResource(R.string.auto_off_hours_1)
                            3 -> stringResource(R.string.auto_off_hours_3)
                            6 -> stringResource(R.string.auto_off_hours_6)
                            else -> stringResource(R.string.auto_off_never)
                        }
                        FilterChip(
                            selected = autoOffHours == hours,
                            onClick = { onSelectAutoOffHours(hours) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGold,
                                selectedLabelColor = BrandNavyBackground
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.auto_off_bank_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Language Switch Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleLanguage),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = BrandMint)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.language_row_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLanguage == "ar") stringResource(R.string.lang_ar) else stringResource(R.string.lang_en),
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandMint
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Subscription Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToSubscribe),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = BrandGold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.subscription_row_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "عرض كود الجهاز وتجديد المفتاح",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SampleRequestCard(
    title: String,
    badgeText: String,
    badgeColor: Color,
    padding: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BrandNavySurfaceVariant)
            .border(1.dp, BrandNavyBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = padding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = TextWhite, fontSize = textSize, fontWeight = FontWeight.Medium)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = badgeText, color = badgeColor, fontSize = textSize, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DriveFlowTheme {
        SettingsScreen(
            currentMarkerSize = MarkerSize.NORMAL,
            onSelectMarkerSize = {},
            barOpacityPercent = 67,
            onChangeBarOpacity = {},
            barPosition = BarPosition(isAuto = true),
            onResetBarPosition = {},
            autoOffHours = 3,
            onSelectAutoOffHours = {},
            currentLanguage = "ar",
            onToggleLanguage = {},
            onNavigateToSubscribe = {}
        )
    }
}
