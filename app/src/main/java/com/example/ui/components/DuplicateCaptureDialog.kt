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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
fun DuplicateCaptureDialog(
    captureText: String,
    existingItem: AnchorItem,
    onOpenExisting: () -> Unit,
    onAddAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface1)
                .drawBehind { drawLine(SurfaceHighlight, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx()) }
                .padding(22.dp)
        ) {
            Text("POSSIBLE DUPLICATE", fontFamily = GeistMono, fontSize = 10.sp, color = AccentGold, letterSpacing = 0.14.em)
            Spacer(Modifier.height(13.dp))
            Text("This looks familiar.", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Light, fontSize = 22.sp, color = TextPrimary)
            Spacer(Modifier.height(7.dp))
            Text("Your new capture is close to an item already saved. You can open the existing one or keep both.",
                fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp, color = TextSecondary)
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Surface2).padding(13.dp)) {
                Text("NEW CAPTURE", fontFamily = GeistMono, fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.12.em)
                Spacer(Modifier.height(4.dp))
                Text(captureText, fontFamily = PlusJakartaSans, fontSize = 13.sp, color = TextPrimary, maxLines = 2)
                Spacer(Modifier.height(12.dp))
                Text("ALREADY SAVED", fontFamily = GeistMono, fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.12.em)
                Spacer(Modifier.height(4.dp))
                Text(existingItem.title, fontFamily = PlusJakartaSans, fontSize = 13.sp, color = TextPrimary, maxLines = 2)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).clickable(onClick = onDismiss).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = GeistMono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 0.1.em)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(Surface2).clickable(onClick = onOpenExisting).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("OPEN EXISTING", fontFamily = GeistMono, fontSize = 10.sp, color = TextSecondary, letterSpacing = 0.08.em)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(AccentGold).clickable(onClick = onAddAnyway).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("ADD ANYWAY", fontFamily = GeistMono, fontSize = 10.sp, color = Surface1, letterSpacing = 0.08.em)
                }
            }
        }
    }
}
