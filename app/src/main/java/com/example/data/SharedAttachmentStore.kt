package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID

/** Keeps shared image data inside app-private storage instead of persisting a temporary grant URI. */
object SharedAttachmentStore {
    private const val TAG = "SharedAttachmentStore"
    private const val ATTACHMENT_DIRECTORY = "shared_attachments"
    private const val MAX_IMAGE_BYTES = 20L * 1024L * 1024L

    fun retain(context: Context, sourceUriText: String): String? {
        val sourceUri = runCatching { Uri.parse(sourceUriText) }.getOrNull() ?: return null
        val mimeType = context.contentResolver.getType(sourceUri) ?: return null
        if (!mimeType.startsWith("image/")) return null

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "img"
        val directory = File(context.filesDir, ATTACHMENT_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) return null
        val target = File(directory, "${UUID.randomUUID()}.$extension")

        return try {
            val input = context.contentResolver.openInputStream(sourceUri) ?: return null
            input.use { source ->
                target.outputStream().buffered().use { output ->
                    val buffer = ByteArray(8192)
                    var copied = 0L
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        copied += read
                        if (copied > MAX_IMAGE_BYTES) throw IOException("Shared image exceeds size limit")
                        output.write(buffer, 0, read)
                    }
                }
            }
            "$ATTACHMENT_DIRECTORY/${target.name}"
        } catch (error: Exception) {
            target.delete()
            Log.w(TAG, "Could not retain shared image: ${error.message}")
            null
        }
    }

    fun contentUri(context: Context, storedPath: String): Uri? {
        // Keep legacy external URIs readable when their original grant is still available.
        if (!storedPath.startsWith("$ATTACHMENT_DIRECTORY/")) {
            return runCatching { Uri.parse(storedPath) }.getOrNull()
        }
        return runCatching {
            val root = File(context.filesDir, ATTACHMENT_DIRECTORY).canonicalFile
            val file = File(context.filesDir, storedPath).canonicalFile
            require(file.path.startsWith(root.path + File.separator) && file.isFile)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.onFailure { Log.w(TAG, "Could not resolve shared image: ${it.message}") }.getOrNull()
    }

    fun delete(context: Context, storedPath: String) {
        if (!storedPath.startsWith("$ATTACHMENT_DIRECTORY/")) return
        runCatching {
            val root = File(context.filesDir, ATTACHMENT_DIRECTORY).canonicalFile
            val file = File(context.filesDir, storedPath).canonicalFile
            require(file.path.startsWith(root.path + File.separator))
            file.delete()
        }.onFailure { Log.w(TAG, "Could not remove shared image: ${it.message}") }
    }
}
