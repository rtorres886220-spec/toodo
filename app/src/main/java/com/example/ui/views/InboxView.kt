package com.example.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.components.AnchorCard
import com.example.ui.components.QuietEmptyState
import com.example.ui.components.QuietHairline
import com.example.ui.theme.GeistMono
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral

@Composable
fun InboxView(
    inboxItems: List<AnchorItem>,
    overdueItems: List<AnchorItem>,
    onMoveToToday: (AnchorItem) -> Unit,
    onSnooze: (AnchorItem) -> Unit,
    onComplete: (AnchorItem) -> Unit,
    onDelete: (AnchorItem) -> Unit,
    onEdit: (AnchorItem) -> Unit,
    onSnoozeCoach: (AnchorItem) -> Unit,
    onAiTriage: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Pinned Overdue Anchors
        if (overdueItems.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OVERDUE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = UrgencyCoral,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "${overdueItems.size}",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = UrgencyCoral,
                        letterSpacing = 0.14.em
                    )
                }
            }
            items(overdueItems, key = { "inbox_overdue_${it.id}" }) { item ->
                AnchorCard(
                    item = item,
                    isOverdue = true,
                    onMoveToToday = { onMoveToToday(item) },
                    onSnooze = { onSnooze(item) },
                    onComplete = { onComplete(item) },
                    onDelete = { onDelete(item) },
                    onEdit = { onEdit(item) },
                    onSnoozeCoach = { onSnoozeCoach(item) }
                )
            }
        }

        // Inbox Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UNSORTED CAPTURES",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.14.em
                )
                Text(
                    text = "${inboxItems.size} ITEMS",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    letterSpacing = 0.14.em
                )
                Text(
                    text = "AI TRIAGE ↗",
                    fontFamily = GeistMono,
                    fontSize = 10.sp,
                    color = TextTertiary,
                    letterSpacing = 0.1.em,
                    modifier = Modifier.clickable(onClick = onAiTriage).padding(start = 12.dp).testTag("inbox_ai_triage_button")
                )
            }
            QuietHairline(modifier = Modifier.padding(horizontal = 24.dp))
        }

        if (inboxItems.isEmpty() && overdueItems.isEmpty()) {
            item {
                QuietEmptyState(message = "Nothing in your inbox")
            }
        } else {
            items(inboxItems, key = { it.id }) { item ->
                AnchorCard(
                    item = item,
                    onMoveToToday = { onMoveToToday(item) },
                    onSnooze = { onSnooze(item) },
                    onComplete = { onComplete(item) },
                    onDelete = { onDelete(item) },
                    onEdit = { onEdit(item) },
                    onSnoozeCoach = { onSnoozeCoach(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}
