package com.driveflow.driver.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveflow.driver.BuildConfig
import com.driveflow.driver.R
import com.driveflow.driver.core.di.AppGraph
import com.driveflow.driver.core.model.LicenseStatus
import com.driveflow.driver.license.DeviceHelper
import com.driveflow.driver.ui.theme.BrandGold
import com.driveflow.driver.ui.theme.BrandMint
import com.driveflow.driver.ui.theme.BrandNavyBackground
import com.driveflow.driver.ui.theme.BrandNavyBorder
import com.driveflow.driver.ui.theme.BrandNavySurface
import com.driveflow.driver.ui.theme.BrandNavySurfaceVariant
import com.driveflow.driver.ui.theme.DriveFlowTheme
import com.driveflow.driver.ui.theme.TextMuted
import com.driveflow.driver.ui.theme.TextWhite
import java.net.URLEncoder

data class ChatBubble(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isInstaPayReminder: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    contactName: String,
    contactPhone: String,
    licenseStatus: LicenseStatus,
    chatHistory: List<String>,
    initialRequestType: String = "",
    onAddChatMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var selectedRequestType by remember { mutableStateOf(initialRequestType) }

    val rawDeviceId = DeviceHelper.getDeviceIdHex(context)
    val displayDeviceCode = DeviceHelper.formatDisplayCode(rawDeviceId)

    val quickReplies = listOf(
        stringResource(R.string.qr_paid_activate),
        stringResource(R.string.qr_not_activated),
        stringResource(R.string.qr_renew),
        stringResource(R.string.qr_reader_issue),
        stringResource(R.string.qr_verdict_wrong),
        stringResource(R.string.qr_other)
    )

    val bubbles = remember(chatHistory) {
        chatHistory.map { raw ->
            when {
                raw.startsWith("SYS_INSTAPAY:") -> ChatBubble(
                    text = raw.removePrefix("SYS_INSTAPAY:"),
                    isUser = false,
                    isInstaPayReminder = true
                )
                raw.startsWith("SYS:") -> ChatBubble(
                    text = raw.removePrefix("SYS:"),
                    isUser = false
                )
                else -> ChatBubble(text = raw, isUser = true)
            }
        }
    }

    LaunchedEffect(bubbles.size) {
        if (bubbles.isNotEmpty()) {
            listState.animateScrollToItem(bubbles.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyBackground)
            .testTag("chat_screen")
    ) {
        // Chat Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandNavySurface)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_screen_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.whatsapp_support, AppGraph.ownerWhatsapp),
                style = MaterialTheme.typography.bodySmall,
                color = BrandMint
            )
        }

        // Disclaimer Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandNavySurfaceVariant)
                .border(1.dp, BrandNavyBorder)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.chat_disclaimer),
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bubbles, key = { it.id }) { bubble ->
                if (bubble.isUser) {
                    UserMessageBubble(bubble.text)
                } else if (bubble.isInstaPayReminder) {
                    InstaPayReminderBubble(
                        instapayNum = AppGraph.ownerInstapay,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("InstaPay", AppGraph.ownerInstapay))
                            Toast.makeText(context, context.getString(R.string.instapay_copied), Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    SystemMessageBubble(bubble.text)
                }
            }
        }

        // Quick Reply Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandNavySurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_replies_title),
                color = BrandGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickReplies.forEach { qr ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedRequestType == qr) BrandGold else BrandNavySurfaceVariant
                            )
                            .clickable {
                                selectedRequestType = qr
                                if (messageInput.isBlank()) {
                                    messageInput = qr
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = qr,
                            color = if (selectedRequestType == qr) BrandNavyBackground else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text(stringResource(R.string.chat_input_hint), color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandNavyBorder,
                        focusedContainerColor = BrandNavySurfaceVariant,
                        unfocusedContainerColor = BrandNavySurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = messageInput.trim()
                        if (text.isNotEmpty() || selectedRequestType.isNotEmpty()) {
                            val reqType = selectedRequestType.ifBlank { "استفسار عام" }
                            val isSub = reqType.contains("تفعيل") || reqType.contains("دفعت") || reqType.contains("تجديد")

                            // Build formatted Arabic WhatsApp text
                            val fullMessage = buildString {
                                appendLine("طلب دعم DriveFlow:")
                                appendLine("الكابتن: ${contactName.ifBlank { "غير مسجل" }}")
                                appendLine("الهاتف: ${contactPhone.ifBlank { "غير مسجل" }}")
                                appendLine("كود الجهاز: $displayDeviceCode")
                                appendLine("الإصدار: v${BuildConfig.VERSION_NAME}")
                                appendLine("الحالة: ${licenseStatus.name}")
                                appendLine("نوع الطلب: $reqType")
                                if (text.isNotBlank() && text != reqType) {
                                    appendLine("التفاصيل: $text")
                                }
                                if (isSub) {
                                    appendLine()
                                    appendLine("مرفق إيصال تحويل انستاباي:")
                                }
                            }

                            // Save to local chat
                            onAddChatMessage(text.ifBlank { reqType })
                            onAddChatMessage("SYS:${context.getString(R.string.system_chat_response)}")
                            if (isSub) {
                                onAddChatMessage("SYS_INSTAPAY:${AppGraph.ownerInstapay}")
                            }

                            // Open WhatsApp
                            val encoded = URLEncoder.encode(fullMessage, "UTF-8")
                            val whatsappUrl = "https://wa.me/${AppGraph.ownerWhatsapp}?text=$encoded"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                            try {
                                intent.setPackage("com.whatsapp")
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                    context.startActivity(fallback)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "تعذر فتح تطبيق واتساب", Toast.LENGTH_SHORT).show()
                                }
                            }

                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandGold, RoundedCornerShape(12.dp))
                        .testTag("send_whatsapp_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = BrandNavyBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(RoundedCornerShape(14.dp, 14.dp, 2.dp, 14.dp))
                .background(BrandNavySurfaceVariant)
                .border(1.dp, BrandNavyBorder, RoundedCornerShape(14.dp, 14.dp, 2.dp, 14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun SystemMessageBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 2.dp))
                .background(BrandMint.copy(alpha = 0.12f))
                .border(1.dp, BrandMint.copy(alpha = 0.4f), RoundedCornerShape(14.dp, 14.dp, 14.dp, 2.dp))
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = BrandMint,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun InstaPayReminderBubble(instapayNum: String, onCopy: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = BrandNavySurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رقم انستاباي للتحويل:",
                        color = BrandGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = instapayNum,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCopy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = BrandNavyBackground)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ رقم انستاباي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    DriveFlowTheme {
        ChatScreen(
            contactName = "أحمد",
            contactPhone = "01012345678",
            licenseStatus = LicenseStatus.FREE,
            chatHistory = listOf("دفعت وعايز أفعل", "SYS:الردود تصلك على واتساب", "SYS_INSTAPAY:01122104999"),
            onAddChatMessage = {}
        )
    }
}
