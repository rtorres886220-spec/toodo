package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    item: AnchorItem,
    onSave: (AnchorItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf(item.title) }
    var type by remember { mutableStateOf(item.type) }
    var dueDate by remember { mutableStateOf(item.dueDate ?: "") }
    var dueTime by remember { mutableStateOf(item.dueTime ?: "") }
    var priority by remember { mutableStateOf(item.priority) }
    var tags by remember { mutableStateOf(item.tags) }
    var estimatedMinutes by remember { mutableStateOf(item.estimatedMinutes?.toString() ?: "25") }

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
                    text = "EDIT ANCHOR",
                    fontFamily = GeistMono,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    letterSpacing = 0.14.em
                )

                Text(
                    text = "CANCEL",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title input
            Text(
                text = "CONTENT",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(AccentGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_title_input")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Type Toggle
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "TASK",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (type == AnchorItem.TYPE_TASK) AccentGold else TextTertiary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { type = AnchorItem.TYPE_TASK }
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "NOTE",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (type == AnchorItem.TYPE_NOTE) AccentGold else TextTertiary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { type = AnchorItem.TYPE_NOTE }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Due Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DUE DATE (YYYY-MM-DD)",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuietEditField(value = dueDate, onValueChange = { dueDate = it })
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TIME (HH:MM)",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuietEditField(value = dueTime, onValueChange = { dueTime = it })
                }
            }

            // Quick date chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val cal = java.util.Calendar.getInstance()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = sdf.format(cal.time)
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val tomorrowStr = sdf.format(cal.time)

                Text(
                    text = "TODAY",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (dueDate == todayStr) AccentGold else TextTertiary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { dueDate = todayStr }
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "TOMORROW",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = if (dueDate == tomorrowStr) AccentGold else TextTertiary,
                    letterSpacing = 0.14.em,
                    modifier = Modifier
                        .clickable { dueDate = tomorrowStr }
                        .padding(vertical = 4.dp)
                )
                if (dueDate.isNotEmpty()) {
                    Text(
                        text = "CLEAR DATE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable { dueDate = ""; dueTime = "" }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Priority & Estimated Minutes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRIORITY",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("high", "medium", "low").forEach { pLevel ->
                            val isSelected = priority.equals(pLevel, ignoreCase = true)
                            Text(
                                text = pLevel.uppercase(),
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = if (isSelected) AccentGold else TextTertiary,
                                letterSpacing = 0.14.em,
                                modifier = Modifier
                                    .clickable { priority = pLevel }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EST. MINUTES",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuietEditField(value = estimatedMinutes, onValueChange = { estimatedMinutes = it })
                }
            }


            Spacer(modifier = Modifier.height(18.dp))

            // Tags
            Text(
                text = "TAGS",
                fontFamily = GeistMono,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.14.em
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuietEditField(value = tags, onValueChange = { tags = it })

            Spacer(modifier = Modifier.height(28.dp))

            // Save action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.vibrate8ms(context)
                        onSave(
                            item.copy(
                                title = title.trim(),
                                type = type,
                                dueDate = dueDate.ifBlank { null },
                                dueTime = dueTime.ifBlank { null },
                                priority = priority.lowercase().ifBlank { AnchorItem.PRIORITY_MEDIUM },
                                tags = tags.trim(),
                                estimatedMinutes = estimatedMinutes.toIntOrNull()
                            )
                        )
                    }
                    .testTag("save_edit_button")
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE CHANGES",
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
private fun QuietEditField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}
