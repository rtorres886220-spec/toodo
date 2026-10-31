package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SharedAttachmentStore
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono

@Composable
fun SharedAttachmentButton(storedPath: String?) {
    if (storedPath.isNullOrBlank()) return
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable {
                val uri = SharedAttachmentStore.contentUri(context, storedPath)
                if (uri == null) {
                    Toast.makeText(context, "This shared image is no longer available", Toast.LENGTH_SHORT).show()
                } else {
                    val mimeType = context.contentResolver.getType(uri) ?: "image/*"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching { context.startActivity(intent) }
                        .onFailure {
                            Toast.makeText(context, "No app can open this image", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .semantics {
                contentDescription = "Open shared image"
                role = Role.Button
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "OPEN SHARED IMAGE",
            fontFamily = GeistMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = AccentGold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("↗", fontSize = 16.sp, color = AccentGold)
    }
}
