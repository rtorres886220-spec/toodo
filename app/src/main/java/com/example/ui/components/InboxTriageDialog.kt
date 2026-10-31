package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.ai.InboxTriagePlan
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun InboxTriageDialog(
    plan: InboxTriagePlan?,
    inboxItems: List<AnchorItem>,
    isLoading: Boolean,
    onAccept: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val suggestions = plan?.suggestions.orEmpty().mapNotNull { suggestion ->
        inboxItems.firstOrNull { it.id == suggestion.taskId }?.let { it to suggestion.reason }
    }
    val selectedIds = remember(plan) { mutableStateListOf<Long>().apply { addAll(suggestions.map { it.first.id }) } }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface1)
                .drawBehind {
                    drawLine(SurfaceHighlight, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx())
                }
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
                .testTag("inbox_triage_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INBOX TRIAGE", fontFamily = GeistMono, fontSize = 11.sp, color = AccentGold, letterSpacing = 0.14.em)
                Text("CLOSE", fontFamily = GeistMono, fontSize = 10.sp, color = TextTertiary,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(4.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Make room for today.", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Light,
                fontSize = 22.sp, color = TextPrimary)
            Spacer(Modifier.height(5.dp))
            Text("Anchor picked a few items that may deserve a place in today's focus. Review before moving anything.",
                fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp, color = TextSecondary)
            Spacer(Modifier.height(17.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentGold, strokeWidth = 1.5.dp, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Looking at due dates and priorities…", fontFamily = GeistMono, fontSize = 10.sp, color = TextTertiary)
                    }
                }
            } else if (suggestions.isEmpty()) {
                Text("No suggestions right now. Your inbox is clear or already matches today's plan.",
                    fontFamily = PlusJakartaSans, fontSize = 13.sp, lineHeight = 19.sp, color = TextSecondary,
                    modifier = Modifier.padding(vertical = 20.dp))
            } else {
                suggestions.forEach { (item, reason) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp)).background(Surface2)
                            .clickable {
                                if (item.id in selectedIds) selectedIds.remove(item.id) else selectedIds.add(item.id)
                            }.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.id in selectedIds,
                            onCheckedChange = { checked -> if (checked) selectedIds.add(item.id) else selectedIds.remove(item.id) },
                            colors = CheckboxDefaults.colors(checkedColor = AccentGold, uncheckedColor = TextTertiary)
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 5.dp)) {
                            Text(item.title, fontFamily = PlusJakartaSans, fontSize = 13.sp, color = TextPrimary, maxLines = 2)
                            Spacer(Modifier.height(3.dp))
                            Text(reason, fontFamily = GeistMono, fontSize = 9.sp, color = TextTertiary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = if (isLoading) "BUILDING PLAN…" else "MOVE ${selectedIds.size} TO TODAY",
                fontFamily = GeistMono, fontSize = 11.sp, color = if (!isLoading && selectedIds.isNotEmpty()) AccentGold else TextTertiary,
                letterSpacing = 0.12.em,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Surface2)
                    .clickable(enabled = !isLoading && selectedIds.isNotEmpty()) { onAccept(selectedIds.toList()) }
                    .testTag("accept_inbox_triage_button").padding(vertical = 14.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
