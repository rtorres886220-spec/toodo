package com.example.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.components.AnchorCard
import com.example.ui.components.BentoGridCard
import com.example.ui.components.Kinetic3DAnchorSculpture
import com.example.ui.components.NotchedTabMergeShape
import com.example.ui.components.QuietEmptyState
import com.example.ui.components.getCategoryTint
import com.example.ui.components.sculpted3DSunkenWell
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper
import com.example.ui.util.MotionUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayView(
    todayTasks: List<AnchorItem>,
    overdueTasks: List<AnchorItem>,
    upcomingTasks: List<AnchorItem> = emptyList(),
    inboxTasks: List<AnchorItem> = emptyList(),
    notes: List<AnchorItem> = emptyList(),
    doneTasks: List<AnchorItem> = emptyList(),
    onMoveToToday: (AnchorItem) -> Unit,
    onSnooze: (AnchorItem) -> Unit,
    onComplete: (AnchorItem) -> Unit,
    onDelete: (AnchorItem) -> Unit,
    onEdit: (AnchorItem) -> Unit,
    onTaskClick: (AnchorItem) -> Unit,
    onNoteClick: (AnchorItem) -> Unit = {},
    onRestore: (AnchorItem) -> Unit = {},
    onSnoozeCoach: (AnchorItem) -> Unit,
    onOpenMorningBrief: () -> Unit,
    onOpenEveningReview: () -> Unit,
    onAiTriage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isReducedMotion = remember { MotionUtil.isReducedMotion(context) }
    val compactLayout = configuration.screenWidthDp < 380 || density.fontScale >= 1.3f
    val horizontalInset = if (compactLayout) 16.dp else 24.dp

    // Primary Main Page Filter: All / Today / Inbox / Upcoming / Notes / Done
    var selectedMainFilter by remember { mutableStateOf("All") }
    BackHandler(enabled = selectedMainFilter != "All") {
        selectedMainFilter = "All"
    }

    // Selected Day Offset (null = Today, 1 = Tomorrow, 2 = Day 2, etc.)
    var selectedDayOffset by remember { mutableStateOf<Int?>(null) }
    // Filter pill selection in Today mode: Day / Week / Month
    var selectedHorizonFilter by remember { mutableStateOf("Day") }
    // Tag filter for notes
    var selectedNoteTag by remember { mutableStateOf<String?>(null) }

    val sdfIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayIso = remember { sdfIso.format(Date()) }

    // Date formatting for Hero card
    val headerInfo = remember(selectedDayOffset) {
        val cal = Calendar.getInstance()
        if (selectedDayOffset != null && selectedDayOffset != 0) {
            cal.add(Calendar.DAY_OF_YEAR, selectedDayOffset!!)
            val d = cal.time
            val m = SimpleDateFormat("MMMM", Locale.getDefault()).format(d)
            val num = SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(d)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(d)
            DateHeaderInfo(d, m, num, dayLabel)
        } else {
            val d = cal.time
            val m = SimpleDateFormat("MMMM", Locale.getDefault()).format(d)
            val num = SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(d)
            DateHeaderInfo(d, m, num, "Today")
        }
    }
    val activeMonthName = headerInfo.monthName
    val activeDayNumberStr = headerInfo.dayNumberStr
    val activeTabLabel = headerInfo.tabLabel

    val showingTodayDay = selectedHorizonFilter == "Day" && (selectedDayOffset == null || selectedDayOffset == 0)
    val visibleOverdueTasks = if (showingTodayDay) overdueTasks else emptyList()

    val displayedTasks = remember(todayTasks, upcomingTasks, overdueTasks, selectedHorizonFilter, selectedDayOffset) {
        when (selectedHorizonFilter) {
            "Day" -> {
                if (selectedDayOffset == null || selectedDayOffset == 0) {
                    val overdueIds = overdueTasks.asSequence().map { it.id }.toSet()
                    todayTasks.filterNot { it.id in overdueIds }
                } else {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, selectedDayOffset!!)
                    val targetIso = sdfIso.format(cal.time)
                    (todayTasks + upcomingTasks).filter { it.dueDate == targetIso }.distinctBy { it.id }
                }
            }
            "Week" -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val weekEndIso = sdfIso.format(cal.time)
                (todayTasks + upcomingTasks).filter { item ->
                    val d = item.dueDate ?: todayIso
                    d >= todayIso && d <= weekEndIso
                }.distinctBy { it.id }.sortedWith(compareBy({ it.dueDate ?: todayIso }, { it.dueTime ?: "23:59" }))
            }
            "Month" -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 29)
                val monthEndIso = sdfIso.format(cal.time)
                (todayTasks + upcomingTasks).filter { item ->
                    val d = item.dueDate ?: todayIso
                    d >= todayIso && d <= monthEndIso
                }.distinctBy { it.id }.sortedWith(compareBy({ it.dueDate ?: todayIso }, { it.dueTime ?: "23:59" }))
            }
            else -> todayTasks
        }
    }

    val heroTask = displayedTasks.firstOrNull { it.dueTime != null } ?: displayedTasks.firstOrNull()
    val bentoTasks = if (heroTask != null) displayedTasks.filter { it.id != heroTask.id } else displayedTasks

    val (dueTimeNumeral, amPm) = remember(heroTask?.dueTime) {
        val dt = heroTask?.dueTime
        if (dt.isNullOrBlank()) {
            val cal = Calendar.getInstance()
            val h = cal.get(Calendar.HOUR)
            val hour = if (h == 0) 12 else h
            val min = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MINUTE))
            val ampm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            "$hour:$min" to ampm
        } else {
            try {
                val parts = dt.split(":")
                var hour = parts[0].toInt()
                val min = parts.getOrNull(1) ?: "00"
                val ampm = if (hour >= 12) "PM" else "AM"
                if (hour > 12) hour -= 12
                if (hour == 0) hour = 12
                "$hour:$min" to ampm
            } catch (_: Exception) {
                dt to ""
            }
        }
    }

    val totalPlannedMinutes = remember(displayedTasks) {
        displayedTasks.sumOf { it.estimatedMinutes ?: 25 }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top Accessible Feature Selector Pill Row (Replaces bottom tabs with instant main-page accessibility)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalInset, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterItems = listOf(
                    "All" to null,
                    "Today" to todayTasks.size,
                    "Inbox" to inboxTasks.size,
                    "Upcoming" to upcomingTasks.size,
                    "Notes" to notes.size,
                    "Done" to doneTasks.size
                )

                items(filterItems) { (fName, count) ->
                    val isSelected = selectedMainFilter == fName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (isSelected) Surface2 else Color.Transparent)
                            .drawBehind {
                                drawRoundRect(
                                    color = if (isSelected) AccentGold.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.1f),
                                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                                    style = Stroke(width = if (isSelected) 1.5.dp.toPx() else 1.dp.toPx())
                                )
                            }
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                selectedMainFilter = fName
                            }
                            .semantics {
                                contentDescription = "$fName view"
                                selected = isSelected
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("main_filter_${fName.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Canvas(modifier = Modifier.size(4.dp)) {
                                    drawCircle(color = AccentGold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = fName.uppercase(),
                                fontFamily = GeistMono,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) AccentGold else TextSecondary,
                                letterSpacing = 0.12.em
                            )
                            if (count != null && count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$count",
                                    fontFamily = GeistMono,
                                    fontSize = 10.sp,
                                    color = if (isSelected) AccentGold.copy(alpha = 0.85f) else TextTertiary,
                                    letterSpacing = 0.08.em
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ALL & TODAY VIEW: HERO SECTION ---
        if (selectedMainFilter == "All" || selectedMainFilter == "Today") {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalInset, vertical = 6.dp)
                ) {
                    val dayChips = remember {
                        val cal = Calendar.getInstance()
                        val list = mutableListOf<Pair<Int, String>>()
                        for (i in 1..4) {
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            list.add(i to "${cal.get(Calendar.DAY_OF_MONTH)}")
                        }
                        list
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dayChips.forEach { (offset, dayNum) ->
                                val isChipSelected = selectedDayOffset == offset
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isChipSelected) Surface2 else Color.Transparent)
                                        .drawBehind {
                                            if (isChipSelected) {
                                                drawCircle(
                                                    color = AccentGold,
                                                    radius = size.minDimension / 2f - 1.dp.toPx(),
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                            } else {
                                                drawCircle(
                                                    color = Color.White.copy(alpha = 0.08f),
                                                    radius = size.minDimension / 2f - 1.dp.toPx(),
                                                    style = Stroke(width = 1.dp.toPx())
                                                )
                                            }
                                        }
                                        .clickable {
                                            HapticHelper.vibrate8ms(context)
                                            selectedDayOffset = if (isChipSelected) null else offset
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isChipSelected) FontWeight.Normal else FontWeight.Light,
                                        fontSize = 13.sp,
                                        color = if (isChipSelected) AccentGold else TextSecondary
                                    )
                                }
                            }
                        }

                        // Hero card with 3D Kinetic Sculpture
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .clip(NotchedTabMergeShape(tabWidth = 148.dp, tabHeight = 44.dp, cardRadius = 32.dp))
                                .background(Surface1)
                                .drawBehind {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.7f),
                                        start = Offset(24.dp.toPx(), 1.dp.toPx()),
                                        end = Offset(size.width - 24.dp.toPx(), 1.dp.toPx()),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                                .clickable {
                                    if (heroTask != null) onTaskClick(heroTask)
                                }
                                .padding(horizontal = 22.dp, vertical = 20.dp)
                                .testTag("hero_notched_card")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = activeMonthName.uppercase(),
                                            fontFamily = GeistMono,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = AccentGold,
                                            letterSpacing = 0.16.em
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeDayNumberStr,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Light,
                                            fontSize = if (compactLayout) 28.sp else 38.sp,
                                            lineHeight = if (compactLayout) 32.sp else 42.sp,
                                            color = TextPrimary,
                                            letterSpacing = (-0.02).em
                                        )
                                    }

                                    // Centerpiece: Kinetic 3D Astrolabe Anchor Sculpture
                                    Box(
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Kinetic3DAnchorSculpture(
                                            sizeDp = if (compactLayout) 64.dp else 84.dp,
                                            autoRotate = true
                                        )
                                    }

                                    // Subtle hairline divider separating sculpture from time numeral
                                    Box(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(1.dp)
                                            .background(HairlineColor)
                                    )

                                    // Next task due time numeral
                                    Row(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .padding(start = 12.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = dueTimeNumeral,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Light,
                                            fontSize = if (compactLayout) 32.sp else 44.sp,
                                            lineHeight = if (compactLayout) 36.sp else 46.sp,
                                            color = TextPrimary,
                                            letterSpacing = (-0.02).em
                                        )
                                        if (amPm.isNotEmpty()) {
                                            Text(
                                                text = " $amPm",
                                                fontFamily = GeistMono,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                letterSpacing = 0.14.em,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Subtle divider separating visual telemetry from task action row
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.White.copy(alpha = 0.06f))
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val subText = if (heroTask != null) {
                                        "${heroTask.title.take(if (compactLayout) 20 else 32)} • $totalPlannedMinutes min"
                                    } else {
                                        "${displayedTasks.size} tasks planned"
                                    }
                                    Text(
                                        text = subText,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Daily Brief & Review triggers
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "BRIEF",
                                            fontFamily = GeistMono,
                                            fontSize = 10.sp,
                                            color = AccentGold,
                                            letterSpacing = 0.14.em,
                                            modifier = Modifier
                                                .clickable { onOpenMorningBrief() }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                .testTag("morning_brief_button")
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 10.sp,
                                            color = TextTertiary,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                        Text(
                                            text = "REVIEW",
                                            fontFamily = GeistMono,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            letterSpacing = 0.14.em,
                                            modifier = Modifier
                                                .clickable { onOpenEveningReview() }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                .testTag("evening_review_button")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- OVERDUE SECTION (Shown when overdue tasks exist in All, Today, or Inbox) ---
        if ((selectedMainFilter == "All" || selectedMainFilter == "Today" || selectedMainFilter == "Inbox") && visibleOverdueTasks.isNotEmpty()) {
            item {
                SectionDividerHeader(
                    title = "OVERDUE ALERTS",
                    count = visibleOverdueTasks.size,
                    dotColor = UrgencyCoral
                )
            }
            items(visibleOverdueTasks, key = { "overdue_${it.id}" }) { item ->
                AnchorCard(
                    item = item,
                    isOverdue = true,
                    onMoveToToday = { onMoveToToday(item) },
                    onSnooze = { onSnooze(item) },
                    onComplete = { onComplete(item) },
                    onDelete = { onDelete(item) },
                    onEdit = { onTaskClick(item) },
                    onSnoozeCoach = { onSnoozeCoach(item) }
                )
            }
        }

        // --- INBOX CAPTURES SECTION (Accessible directly from main page in All or Inbox) ---
        if (selectedMainFilter == "Inbox" || (selectedMainFilter == "All" && inboxTasks.isNotEmpty())) {
            item {
                SectionDividerHeader(
                    title = "INBOX CAPTURES",
                    count = inboxTasks.size,
                    dotColor = AccentGold,
                    actionText = "AI TRIAGE ↗",
                    onActionClick = onAiTriage
                )
            }
            if (inboxTasks.isEmpty()) {
                item {
                    QuietEmptyState(message = "Inbox is empty • Captures are sorted")
                }
            } else {
                items(inboxTasks, key = { "inbox_${it.id}" }) { item ->
                    AnchorCard(
                        item = item,
                        onMoveToToday = { onMoveToToday(item) },
                        onSnooze = { onSnooze(item) },
                        onComplete = { onComplete(item) },
                        onDelete = { onDelete(item) },
                        onEdit = { onTaskClick(item) },
                        onSnoozeCoach = { onSnoozeCoach(item) }
                    )
                }
            }
        }

        // --- TODAY'S ACTIVE FOCUS SECTION ---
        if (selectedMainFilter == "All" || selectedMainFilter == "Today") {
            item {
                SectionDividerHeader(
                    title = "TODAY'S ANCHORS",
                    count = displayedTasks.size,
                    dotColor = AccentGold
                )
            }

            // Horizon filter pills in Today-only mode
            if (selectedMainFilter == "Today") {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalInset, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Day", "Week", "Month").forEach { horizon ->
                            val isSel = selectedHorizonFilter == horizon
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(if (isSel) Surface2 else Color.Transparent)
                                    .clickable {
                                        HapticHelper.vibrate8ms(context)
                                        selectedHorizonFilter = horizon
                                        if (horizon != "Day") selectedDayOffset = null
                                    }
                                    .drawBehind {
                                        if (isSel) {
                                            drawLine(
                                                color = SurfaceHighlight,
                                                start = Offset(0f, 0.5f),
                                                end = Offset(size.width, 0.5f),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = horizon,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = if (isSel) FontWeight.Normal else FontWeight.Light,
                                    fontSize = 13.sp,
                                    color = if (isSel) AccentGold else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (bentoTasks.isEmpty()) {
                item {
                    QuietEmptyState(message = "Nothing planned for today")
                }
            } else if (compactLayout || selectedMainFilter == "All") {
                // In compact or unified All view, render clean full-width cards with separation
                items(bentoTasks, key = { "today_task_${it.id}" }) { item ->
                    AnchorCard(
                        item = item,
                        onMoveToToday = { onMoveToToday(item) },
                        onSnooze = { onSnooze(item) },
                        onComplete = { onComplete(item) },
                        onDelete = { onDelete(item) },
                        onEdit = { onTaskClick(item) },
                        onSnoozeCoach = { onSnoozeCoach(item) }
                    )
                }
            } else {
                // 2-column bento layout in Today mode
                val pairs = bentoTasks.chunked(2)
                pairs.forEachIndexed { rowIndex, pair ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalInset, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val leftItem = pair[0]
                            val leftTint = getCategoryTint(leftItem)
                            val leftHeight = if (rowIndex % 2 == 0) 170.dp else 140.dp

                            Box(modifier = Modifier.weight(1f)) {
                                BentoGridCard(
                                    item = leftItem,
                                    tint = leftTint,
                                    minHeight = leftHeight,
                                    staggerIndex = rowIndex * 2,
                                    onCardClick = { onTaskClick(leftItem) },
                                    onReminderClick = { onSnooze(leftItem) }
                                )
                            }

                            if (pair.size > 1) {
                                val rightItem = pair[1]
                                val rightTint = getCategoryTint(rightItem)
                                val rightHeight = if (rowIndex % 2 == 0) 140.dp else 170.dp

                                Box(modifier = Modifier.weight(1f)) {
                                    BentoGridCard(
                                        item = rightItem,
                                        tint = rightTint,
                                        minHeight = rightHeight,
                                        staggerIndex = rowIndex * 2 + 1,
                                        onCardClick = { onTaskClick(rightItem) },
                                        onReminderClick = { onSnooze(rightItem) }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // --- UPCOMING SCHEDULE SECTION ---
        if (selectedMainFilter == "Upcoming" || (selectedMainFilter == "All" && upcomingTasks.isNotEmpty())) {
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowStr = dateFormat.format(cal.time)

            val strictlyUpcoming = upcomingTasks.filter { it.dueDate != null && it.dueDate > todayStr }
            val tomorrowTasks = strictlyUpcoming.filter { it.dueDate == tomorrowStr }
            val laterTasks = strictlyUpcoming.filter { it.dueDate != null && it.dueDate > tomorrowStr }

            item {
                SectionDividerHeader(
                    title = "UPCOMING SCHEDULE",
                    count = strictlyUpcoming.size,
                    dotColor = Color(0xFF64B5F6)
                )
            }

            if (strictlyUpcoming.isEmpty()) {
                item {
                    QuietEmptyState(message = "Nothing upcoming on the calendar")
                }
            } else {
                if (tomorrowTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "TOMORROW // ${tomorrowTasks.size} TASKS",
                            fontFamily = GeistMono,
                            fontSize = 10.sp,
                            color = AccentGold,
                            letterSpacing = 0.14.em,
                            modifier = Modifier.padding(horizontal = horizontalInset, vertical = 6.dp)
                        )
                    }
                    items(tomorrowTasks, key = { "tomorrow_${it.id}" }) { item ->
                        AnchorCard(
                            item = item,
                            onMoveToToday = { onMoveToToday(item) },
                            onSnooze = { onSnooze(item) },
                            onComplete = { onComplete(item) },
                            onDelete = { onDelete(item) },
                            onEdit = { onTaskClick(item) },
                            onSnoozeCoach = { onSnoozeCoach(item) }
                        )
                    }
                }

                if (laterTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "LATER THIS WEEK & BEYOND // ${laterTasks.size} TASKS",
                            fontFamily = GeistMono,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            letterSpacing = 0.14.em,
                            modifier = Modifier.padding(horizontal = horizontalInset, vertical = 6.dp)
                        )
                    }
                    items(laterTasks, key = { "later_${it.id}" }) { item ->
                        AnchorCard(
                            item = item,
                            onMoveToToday = { onMoveToToday(item) },
                            onSnooze = { onSnooze(item) },
                            onComplete = { onComplete(item) },
                            onDelete = { onDelete(item) },
                            onEdit = { onTaskClick(item) },
                            onSnoozeCoach = { onSnoozeCoach(item) }
                        )
                    }
                }
            }
        }

        // --- SAVED NOTES & IDEAS SECTION ---
        if (selectedMainFilter == "Notes" || (selectedMainFilter == "All" && notes.isNotEmpty())) {
            val allTags = notes.flatMap { it.tagList }.distinct()
            val filteredNotes = if (selectedNoteTag == null) notes else notes.filter { it.tagList.contains(selectedNoteTag) }

            item {
                SectionDividerHeader(
                    title = "SAVED NOTES & IDEAS",
                    count = notes.size,
                    dotColor = Color(0xFFBA68C8)
                )
            }

            // Tag selector chips
            if (allTags.isNotEmpty() && selectedMainFilter == "Notes") {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalInset, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "ALL",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = if (selectedNoteTag == null) AccentGold else TextTertiary,
                                letterSpacing = 0.12.em,
                                modifier = Modifier
                                    .clickable { selectedNoteTag = null }
                                    .padding(vertical = 6.dp)
                            )
                        }
                        items(allTags) { tag ->
                            val isSel = selectedNoteTag == tag
                            Text(
                                text = "#${tag.uppercase()}",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = if (isSel) AccentGold else TextTertiary,
                                letterSpacing = 0.12.em,
                                modifier = Modifier
                                    .clickable { selectedNoteTag = if (isSel) null else tag }
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                item {
                    QuietEmptyState(message = "No notes saved yet")
                }
            } else {
                items(filteredNotes.take(if (selectedMainFilter == "All") 4 else 100), key = { "note_summary_${it.id}" }) { note ->
                    NoteSummaryCard(
                        note = note,
                        onClick = { onNoteClick(note) },
                        modifier = Modifier.padding(horizontal = horizontalInset, vertical = 5.dp)
                    )
                }
            }
        }

        // --- COMPLETED ARCHIVE SECTION ---
        if (selectedMainFilter == "Done" || (selectedMainFilter == "All" && doneTasks.isNotEmpty())) {
            item {
                SectionDividerHeader(
                    title = "COMPLETED ARCHIVE",
                    count = doneTasks.size,
                    dotColor = Color(0xFF81C784)
                )
            }

            if (doneTasks.isEmpty()) {
                item {
                    QuietEmptyState(message = "No completed tasks yet")
                }
            } else {
                items(doneTasks.take(if (selectedMainFilter == "All") 5 else 100), key = { "done_${it.id}" }) { item ->
                    AnchorCard(
                        item = item,
                        onMoveToToday = { onRestore(item) },
                        onSnooze = { },
                        onComplete = { onRestore(item) },
                        onDelete = { onDelete(item) },
                        onEdit = { onTaskClick(item) },
                        onSnoozeCoach = { }
                    )
                }
            }
        }

        // Bottom space so scrolling content is never occluded by floating CaptureBar
        item {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

// Clean section header with indicator dot, GeistMono title, count chip, and trailing hairline divider
@Composable
fun SectionDividerHeader(
    title: String,
    count: Int? = null,
    dotColor: Color = AccentGold,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = dotColor)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = GeistMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextSecondary,
            letterSpacing = 0.14.em
        )
        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Surface2)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    fontFamily = GeistMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (dotColor == UrgencyCoral) UrgencyCoral else TextSecondary,
                    letterSpacing = 0.08.em
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))

        // Trailing horizontal divider providing clean hints of separation from content below
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onActionClick() }
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = actionText,
                    fontFamily = GeistMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = AccentGold,
                    letterSpacing = 0.1.em
                )
            }
        }
    }
}

// Clean note preview card with explicit separation between title, content, and tags
@Composable
fun NoteSummaryCard(
    note: AnchorItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface1)
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("note_item_${note.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Surface2)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "NOTE",
                    fontFamily = GeistMono,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentGold,
                    letterSpacing = 0.1.em
                )
            }
        }

        // Clean 1px hairline divider separating note title from content
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )

        if (note.rawInput.isNotBlank()) {
            Text(
                text = note.rawInput,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (note.tagList.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                note.tagList.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            fontFamily = GeistMono,
                            fontSize = 10.sp,
                            color = TextTertiary,
                            letterSpacing = 0.08.em
                        )
                    }
                }
            }
        }
    }
}

private data class DateHeaderInfo(
    val date: Date,
    val monthName: String,
    val dayNumberStr: String,
    val tabLabel: String
)
