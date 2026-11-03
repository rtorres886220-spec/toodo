package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.GoldButtonText
import com.example.ui.theme.GoldButtonBackground
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SoftDarkBackground
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import java.util.Locale

@Composable
fun TaskDetailSheet(
    item: AnchorItem,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    onToggleSubtask: (Int, Boolean) -> Unit = { _, _ -> },
    onAddSubtask: (String) -> Unit = {},
    onGenerateSteps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tint = remember(item) { getCategoryTint(item) }
    var newStepInput by remember { mutableStateOf("") }

    // Header date formatting: e.g. "June 15 \n Tuesday"
    val (dateDisplayStr, weekdayDisplayStr) = remember(item.dueDate, item.createdAt) {
        try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = item.dueDate?.let { sdfIn.parse(it) } ?: Date(item.createdAt)
            val monthDay = SimpleDateFormat("MMMM d", Locale.getDefault()).format(date)
            val weekday = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
            monthDay to weekday
        } catch (_: Exception) {
            "Today" to SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftDarkBackground.copy(alpha = 0.94f))
            .clickable(onClick = onDismiss) // tap backdrop to dismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {} // block click propagation
        ) {
            // Tinted color header: large date in light type + thin line icon at right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(brush = tint.gradientBrush)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Back arrow top-left
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            onDismiss()
                        }
                        .align(Alignment.TopStart)
                        .testTag("detail_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val stroke = 1.5.dp.toPx()
                        drawLine(
                            color = tint.foreground,
                            start = Offset(size.width * 0.65f, size.height * 0.25f),
                            end = Offset(size.width * 0.35f, size.height * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = tint.foreground,
                            start = Offset(size.width * 0.35f, size.height * 0.5f),
                            end = Offset(size.width * 0.65f, size.height * 0.75f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Header right: thin line category icon
                Canvas(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = 6.dp)
                ) {
                    drawCategoryIcon(tint.iconType, color = tint.foreground, strokeWidth = 1.75.dp.toPx())
                }

                // Large date in light type in tint foreground
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 28.dp)
                ) {
                    Text(
                        text = dateDisplayStr,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        color = tint.foreground,
                        letterSpacing = (-0.02).em
                    )
                    Text(
                        text = weekdayDisplayStr,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        color = tint.foreground.copy(alpha = 0.85f),
                        letterSpacing = (-0.02).em
                    )
                }
            }

            // Dark surface sheet rising from below with a 32px top radius and concave notch at top-right
            // cradling a circular 56px gold edit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = (-20).dp) // slightly tuck under header for seamless overlap
            ) {
                // Sheet container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(NotchedSheetWithCradleShape(topRadius = 32.dp, buttonCenterOffsetRight = 56.dp, cradleRadius = 38.dp))
                        .background(Surface1)
                        .drawBehind {
                            // 1px top highlight
                            drawLine(
                                color = SurfaceHighlight,
                                start = Offset(32.dp.toPx(), 0.5f),
                                end = Offset(size.width - 120.dp.toPx(), 0.5f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 36.dp, bottom = 48.dp)
                        .navigationBarsPadding()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SharedAttachmentButton(item.sharedUri)
                        if (item.parentId != null) {
                            Text(
                                text = "SUBTASK // LINKED TO PARENT ANCHOR",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = AccentGold,
                                letterSpacing = 0.14.em
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        // Title
                        Text(
                            text = item.title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            color = TextPrimary
                        )

                        // Description / raw input
                        val descText = item.rawInput.ifBlank { "No additional notes or instructions provided." }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = descText,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hairline divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(HairlineColor)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Metadata Rows:
                        // 1. Color dot and name
                        MetadataRow(
                            icon = {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawCircle(color = tint.foreground)
                                }
                            },
                            label = "Color",
                            value = tint.name
                        )

                        // 2. Start time arrow end time with duration in muted text
                        val timeStr = buildString {
                            val start = item.dueTime ?: "09:00 AM"
                            append(start)
                            append("  →  ")
                            val duration = item.estimatedMinutes ?: 45
                            append("Duration: ${duration}m")
                        }
                        MetadataRow(
                            icon = {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    drawCircle(
                                        color = TextSecondary,
                                        radius = size.minDimension / 2f - 1.dp.toPx(),
                                        style = Stroke(width = 1.25.dp.toPx())
                                    )
                                    drawLine(
                                        color = TextSecondary,
                                        start = Offset(size.width / 2f, size.height / 2f),
                                        end = Offset(size.width / 2f, size.height * 0.25f),
                                        strokeWidth = 1.25.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            },
                            label = "Schedule",
                            value = timeStr
                        )

                        // 3. All - day toggle / status
                        MetadataRow(
                            icon = {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val stroke = 1.25.dp.toPx()
                                    drawCircle(
                                        color = TextSecondary,
                                        radius = size.minDimension / 2f - 2f,
                                        style = Stroke(width = stroke)
                                    )
                                    drawArc(
                                        color = TextSecondary,
                                        startAngle = -90f,
                                        sweepAngle = 180f,
                                        useCenter = true
                                    )
                                }
                            },
                            label = "Sprint Mode",
                            value = if (item.status == AnchorItem.STATUS_TODAY) "Active Today" else "Queued"
                        )

                        if (!item.recurrenceRule.isNullOrBlank()) {
                            MetadataRow(
                                icon = {
                                    Canvas(modifier = Modifier.size(18.dp)) {
                                        drawCircle(
                                            color = AccentGold,
                                            radius = size.minDimension / 2f - 1.dp.toPx(),
                                            style = Stroke(width = 1.25.dp.toPx())
                                        )
                                    }
                                },
                                label = "Repeats",
                                value = item.recurrenceRule
                            )
                        }

                        // 4. Repeat rule
                        MetadataRow(
                            icon = {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val stroke = 1.25.dp.toPx()
                                    drawArc(
                                        color = TextSecondary,
                                        startAngle = 30f,
                                        sweepAngle = 280f,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                }
                            },
                            label = "Cadence",
                            value = if (item.priority == AnchorItem.PRIORITY_HIGH) "Priority anchor" else "Standard focus"
                        )

                        // 5. Interactive Micro-steps Checklist
                        Spacer(modifier = Modifier.height(20.dp))
                        val rawSteps = remember(item.aiBreakdownSteps) {
                            item.aiBreakdownSteps?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
                        }
                        val parsedSteps = remember(rawSteps) {
                            rawSteps.map { line ->
                                val isChecked = line.startsWith("[x]", ignoreCase = true)
                                val text = line.replace(Regex("^\\[[ xX]\\]\\s*"), "").trim()
                                isChecked to text
                            }
                        }
                        val completedCount = parsedSteps.count { it.first }
                        val totalSteps = parsedSteps.size
                        val progressFraction = if (totalSteps > 0) completedCount.toFloat() / totalSteps else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Micro-steps",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                if (totalSteps > 0) {
                                    Text(
                                        text = "$completedCount of $totalSteps completed",
                                        fontFamily = GeistMono,
                                        fontSize = 11.sp,
                                        color = TextTertiary,
                                        letterSpacing = 0.14.em
                                    )
                                }
                            }

                            // Dynamic completion ring
                            Canvas(modifier = Modifier.size(28.dp)) {
                                val stroke = 1.75.dp.toPx()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.minDimension / 2f - 2.dp.toPx()

                                drawCircle(
                                    color = TextTertiary.copy(alpha = 0.3f),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = stroke)
                                )

                                if (progressFraction > 0f) {
                                    drawArc(
                                        color = AccentGold,
                                        startAngle = -90f,
                                        sweepAngle = progressFraction * 360f,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✦  ASK ANCHOR FOR A FIRST-STEP PLAN",
                            fontFamily = GeistMono,
                            fontSize = 10.sp,
                            color = AccentGold,
                            letterSpacing = 0.1.em,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onGenerateSteps()
                                }
                                .testTag("generate_task_steps_button")
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render each checklist item
                        parsedSteps.forEachIndexed { index, (isStepDone, stepTitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        HapticHelper.vibrate8ms(context)
                                        onToggleSubtask(index, !isStepDone)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 20px check indicator
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val stroke = 1.25.dp.toPx()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = size.minDimension / 2f - 2f

                                    drawCircle(
                                        color = if (isStepDone) AccentGold else TextTertiary,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = stroke)
                                    )

                                    if (isStepDone) {
                                        drawCircle(
                                            color = AccentGold.copy(alpha = 0.2f),
                                            radius = radius,
                                            center = center
                                        )
                                        drawLine(
                                            color = AccentGold,
                                            start = Offset(center.x - 4.dp.toPx(), center.y),
                                            end = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()),
                                            strokeWidth = stroke,
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = AccentGold,
                                            start = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()),
                                            end = Offset(center.x + 4.dp.toPx(), center.y - 3.dp.toPx()),
                                            strokeWidth = stroke,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = stepTitle,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = if (isStepDone) TextTertiary else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Add micro-step input row
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
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
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = newStepInput,
                                onValueChange = { newStepInput = it },
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(AccentGold),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (newStepInput.isNotBlank()) {
                                        HapticHelper.vibrate8ms(context)
                                        onAddSubtask(newStepInput.trim())
                                        newStepInput = ""
                                    }
                                }),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (newStepInput.isEmpty()) {
                                        Text(
                                            text = "+ Add a 5-minute micro step...",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Light,
                                            fontSize = 13.sp,
                                            color = TextTertiary
                                        )
                                    }
                                    inner()
                                }
                            )

                            if (newStepInput.isNotBlank()) {
                                Text(
                                    text = "ADD",
                                    fontFamily = GeistMono,
                                    fontSize = 11.sp,
                                    color = AccentGold,
                                    letterSpacing = 0.14.em,
                                    modifier = Modifier
                                        .clickable {
                                            HapticHelper.vibrate8ms(context)
                                            onAddSubtask(newStepInput.trim())
                                            newStepInput = ""
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }


                        // Complete Action button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onComplete()
                                    onDismiss()
                                }
                                .drawBehind {
                                    drawLine(
                                        color = SurfaceHighlight,
                                        start = Offset(26.dp.toPx(), 0.5f),
                                        end = Offset(size.width - 26.dp.toPx(), 0.5f),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .testTag("detail_complete_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "COMPLETE ANCHOR",
                                fontFamily = GeistMono,
                                fontWeight = FontWeight.Light,
                                fontSize = 12.sp,
                                color = AccentGold,
                                letterSpacing = 0.14.em
                            )
                        }
                    }
                }

                // Concave notch at the sheet's top-right cradles a circular 56px gold edit button!
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-28).dp, y = (-28).dp)
                        .clip(CircleShape)
                        .background(GoldButtonBackground)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            onEdit()
                        }
                        .drawBehind {
                            // 1px top highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.35f),
                                radius = size.minDimension / 2f - 1.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        .testTag("detail_edit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Thin pencil glyph in dark gold color #1A1407
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val stroke = 1.75.dp.toPx()
                        val w = size.width
                        val h = size.height
                        // Slanted pencil line
                        drawLine(
                            color = GoldButtonText,
                            start = Offset(w * 0.28f, h * 0.72f),
                            end = Offset(w * 0.68f, h * 0.32f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = GoldButtonText,
                            start = Offset(w * 0.28f, h * 0.72f),
                            end = Offset(w * 0.22f, h * 0.78f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Light,
            fontSize = 15.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
