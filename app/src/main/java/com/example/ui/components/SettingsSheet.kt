package com.example.ui.components

import com.example.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    morningTime: String,
    eveningTime: String,
    quietHours: String,
    escalationIntensity: String,
    deadAppHours: Int,
    notificationsAllowed: Boolean,
    exactAlarmsAllowed: Boolean,
    onSave: (morning: String, evening: String, quiet: String, intensity: String, deadAppH: Int) -> Unit,
    onTestNotification: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onLoadSampleData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var morning by remember { mutableStateOf(morningTime) }
    var evening by remember { mutableStateOf(eveningTime) }
    var quiet by remember { mutableStateOf(quietHours) }
    var intensity by remember { mutableStateOf(escalationIntensity) }
    var deadApp by remember { mutableStateOf(deadAppHours) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface1,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(3.dp)
                    .drawBehind { drawRect(color = HairlineColor) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .drawBehind {
                    drawLine(
                        color = SurfaceHighlight,
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS",
                    fontFamily = GeistMono,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    letterSpacing = 0.14.em
                )

                Text(
                    text = "CLOSE",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .heightIn(min = 48.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Morning & Evening Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MORNING BRIEF",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuietInputField(label = "Morning brief time, 24 hour clock", value = morning, onValueChange = { morning = it })
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EVENING REVIEW",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuietInputField(label = "Evening review time, 24 hour clock", value = evening, onValueChange = { evening = it })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quiet Hours
            Text(
                text = "QUIET HOURS",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuietInputField(label = "Quiet hours start and end, 24 hour clock", value = quiet, onValueChange = { quiet = it })

            Spacer(modifier = Modifier.height(22.dp))

            // Escalation Intensity
            Text(
                text = "ESCALATION INTENSITY",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                listOf("Gentle", "Standard", "Persistent").forEach { level ->
                    val isSelected = intensity.equals(level, ignoreCase = true)
                    Text(
                        text = level.uppercase(),
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = if (isSelected) AccentGold else TextTertiary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                intensity = level
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Dead-App Check-In
            Text(
                text = "INACTIVITY NUDGE",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                listOf(0 to "OFF", 6 to "6H", 12 to "12H", 24 to "24H").forEach { (h, label) ->
                    val isSelected = deadApp == h
                    Text(
                        text = label,
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = if (isSelected) AccentGold else TextTertiary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                deadApp = h
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "NOTIFICATION DELIVERY",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Text(
                text = "Reminders can arrive later when exact alarm access is off.",
                fontSize = 13.sp,
                color = TextTertiary,
                modifier = Modifier.padding(top = 5.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clickable { onRequestNotificationPermission() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", fontSize = 14.sp, color = TextPrimary)
                Text(
                    if (notificationsAllowed) "ENABLED" else "ENABLE",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (notificationsAllowed) TextSecondary else AccentGold,
                    letterSpacing = 0.12.em
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clickable { onRequestExactAlarmAccess() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Precise reminder timing", fontSize = 14.sp, color = TextPrimary)
                Text(
                    if (exactAlarmsAllowed) "AVAILABLE" else "ALLOW",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (exactAlarmsAllowed) TextSecondary else AccentGold,
                    letterSpacing = 0.12.em
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            if (BuildConfig.DEBUG) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            onLoadSampleData()
                        }
                        .testTag("load_sample_data_button")
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOAD SAMPLE DATA",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = AccentGold,
                        letterSpacing = 0.14.em
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Test Notification action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.vibrate8ms(context)
                        onTestNotification()
                    }
                    .testTag("test_notification_button")
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DISPATCH TEST NUDGE",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.14.em
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            validationError?.let { message ->
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = UrgencyCoral,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Save Settings action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable {
                        HapticHelper.vibrate8ms(context)
                        val clockPattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")
                        val quietParts = quiet.split("-").map { it.trim() }
                        validationError = when {
                            !clockPattern.matches(morning.trim()) -> "Enter the morning time as HH:mm (00:00–23:59)."
                            !clockPattern.matches(evening.trim()) -> "Enter the evening time as HH:mm (00:00–23:59)."
                            quietParts.size != 2 || quietParts.any { !clockPattern.matches(it) } ->
                                "Enter quiet hours as HH:mm - HH:mm."
                            else -> null
                        }
                        if (validationError == null) onSave(morning.trim(), evening.trim(), quietParts.joinToString(" - "), intensity, deadApp)
                    }
                    .testTag("save_settings_button")
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE SETTINGS",
                    fontFamily = GeistMono,
                    fontSize = 12.sp,
                    color = AccentGold,
                    letterSpacing = 0.14.em
                )
            }
        }
    }
}

@Composable
private fun QuietInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .drawBehind {
                drawLine(
                    color = HairlineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .padding(vertical = 6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = GeistMono,
                fontSize = 13.sp,
                color = TextPrimary,
                letterSpacing = 0.14.em
            ),
            singleLine = true,
            cursorBrush = SolidColor(AccentGold),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label }
        )
    }
}
