package com.example.ui.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.components.BentoGridCard
import com.example.ui.components.QuietEmptyState
import com.example.ui.components.QuietHairline
import com.example.ui.components.getCategoryTint
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.TextTertiary

@Composable
fun NotesView(
    notes: List<AnchorItem>,
    onDelete: (AnchorItem) -> Unit,
    onEdit: (AnchorItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compactLayout = LocalConfiguration.current.screenWidthDp < 380 || LocalDensity.current.fontScale >= 1.3f
    val horizontalInset = if (compactLayout) 16.dp else 24.dp
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val allTags = notes.flatMap { it.tagList }.distinct()
    val filteredNotes = if (selectedTag == null) notes else notes.filter { it.tagList.contains(selectedTag) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Tag filters
        if (allTags.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalInset, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "ALL",
                            fontFamily = GeistMono,
                            fontSize = 11.sp,
                            color = if (selectedTag == null) AccentGold else TextTertiary,
                            letterSpacing = 0.14.em,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { selectedTag = null }
                                .semantics {
                                    contentDescription = "Show all notes"
                                    selected = selectedTag == null
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                    items(allTags) { tag ->
                        val isSelected = selectedTag == tag
                        Text(
                            text = "#${tag.uppercase()}",
                            fontFamily = GeistMono,
                            fontSize = 11.sp,
                            color = if (isSelected) AccentGold else TextTertiary,
                            letterSpacing = 0.14.em,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { selectedTag = if (isSelected) null else tag }
                                .semantics {
                                    contentDescription = "Filter notes by $tag"
                                    selected = isSelected
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
                QuietHairline(modifier = Modifier.padding(horizontal = horizontalInset))
            }
        }

        if (filteredNotes.isEmpty()) {
            item {
                QuietEmptyState(message = "No notes yet")
            }
        } else if (compactLayout) {
            items(filteredNotes, key = { "note_${it.id}" }) { item ->
                BentoGridCard(
                    item = item,
                    tint = getCategoryTint(item),
                    minHeight = 142.dp,
                    onCardClick = { onEdit(item) },
                    onReminderClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Anchor Note", item.title))
                        Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = horizontalInset, vertical = 6.dp)
                )
            }
        } else {
            val pairs = filteredNotes.chunked(2)
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
                        val leftHeight = if (rowIndex % 2 == 0) 165.dp else 140.dp

                        Box(modifier = Modifier.weight(1f)) {
                            BentoGridCard(
                                item = leftItem,
                                tint = leftTint,
                                minHeight = leftHeight,
                                staggerIndex = rowIndex * 2,
                                onCardClick = { onEdit(leftItem) },
                                onReminderClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Anchor Note", leftItem.title)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (pair.size > 1) {
                            val rightItem = pair[1]
                            val rightTint = getCategoryTint(rightItem)
                            val rightHeight = if (rowIndex % 2 == 0) 140.dp else 165.dp

                            Box(modifier = Modifier.weight(1f)) {
                                BentoGridCard(
                                    item = rightItem,
                                    tint = rightTint,
                                    minHeight = rightHeight,
                                    staggerIndex = rowIndex * 2 + 1,
                                    onCardClick = { onEdit(rightItem) },
                                    onReminderClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Anchor Note", rightItem.title)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}
