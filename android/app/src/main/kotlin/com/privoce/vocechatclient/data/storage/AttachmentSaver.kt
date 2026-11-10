package com.privoce.vocechatclient.data.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.privoce.vocechatclient.data.api.VoceApiClient
import com.privoce.vocechatclient.data.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * 附件保存器：把聊天附件保存到公共下载目录
 * `Download/VoceChat/{Images|Videos|Audios|Files}/文件名`
 *
 * Android 10（API 29）及以上通过 MediaStore 写入共享目录，无需存储权限；
 * Android 9 及以下直接写入公共目录，需要 WRITE_EXTERNAL_STORAGE 运行时权限。
 */
object AttachmentSaver {

    private const val TAG = "AttachmentSaver"

    /** 公共下载目录下的应用子目录 */
    const val ROOT_FOLDER = "VoceChat"

    /** 媒体文件夹名 */
    const val FOLDER_IMAGES = "Images"
    const val FOLDER_VIDEOS = "Videos"
    const val FOLDER_AUDIOS = "Audios"
    const val FOLDER_FILES = "Files"

    /** 文件名中的非法字符 */
    private val ILLEGAL_CHARS = Regex("[\\\\/:*?\"<>|\\x00-\\x1F]")

    /** 保存结果 */
    data class SavedAttachment(
        /** 便于用户理解的相对路径，如 Download/VoceChat/Images/photo.jpg */
        val displayPath: String,
        /** 可直接访问的文件（API 29 以下）；API 29+ 由 MediaStore 管理，故为 null */
        val file: File? = null
    )

    /** 按附件类型决定媒体文件夹 */
    fun folderFor(message: ChatMessage): String = when {
        message.isImage -> FOLDER_IMAGES
        message.isVideo -> FOLDER_VIDEOS
        message.isAudio -> FOLDER_AUDIOS
        else -> FOLDER_FILES
    }

    /** 推断附件 MIME 类型 */
    fun mimeTypeFor(message: ChatMessage): String {
        // 服务端在 properties.content_type 里给出真实类型
        val inner = message.properties?.get("content_type")?.toString()
        if (!inner.isNullOrBlank()) return inner
        val contentType = message.contentType
        if (contentType.contains('/') && !contentType.startsWith("vocechat/")) return contentType
        return mimeTypeFromName(message.fileName)
    }

    /** 保存服务器上的附件（图片/语音/视频/文件） */
    suspend fun saveRemoteAttachment(
        context: Context,
        apiClient: VoceApiClient,
        message: ChatMessage
    ): Result<SavedAttachment> = withContext(Dispatchers.IO) {
        val folder = folderFor(message)
        val mime = mimeTypeFor(message)
        val baseName = fileNameFor(message.fileName, mime)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$ROOT_FOLDER/$folder"
                val name = uniqueMediaStoreName(context, baseName, relativePath)
                val uri = insertPending(context, name, mime, relativePath)
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        apiClient.downloadFileToStream(message.content, output).getOrThrow()
                    } ?: throw IOException("Cannot open output stream for $name")
                    publishPending(context, uri)
                    Result.success(SavedAttachment(displayPath(folder, name)))
                } catch (e: Exception) {
                    context.contentResolver.delete(uri, null, null)
                    throw e
                }
            } else {
                val dir = publicFolderDir(folder)
                val name = uniqueFileName(dir, baseName)
                val dest = File(dir, name)
                apiClient.downloadFile(message.content, dest).getOrThrow()
                Result.success(SavedAttachment(displayPath(folder, name), dest))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveRemoteAttachment failed", e)
            Result.failure(e)
        }
    }

    /** 保存本地已有文件（如已下载到缓存的 PDF） */
    suspend fun saveLocalFile(
        context: Context,
        source: File,
        folder: String,
        fileName: String,
        mimeType: String
    ): Result<SavedAttachment> = withContext(Dispatchers.IO) {
        val baseName = fileNameFor(fileName, mimeType)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$ROOT_FOLDER/$folder"
                val name = uniqueMediaStoreName(context, baseName, relativePath)
                val uri = insertPending(context, name, mimeType, relativePath)
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        source.inputStream().use { input -> input.copyTo(output) }
                    } ?: throw IOException("Cannot open output stream for $name")
                    publishPending(context, uri)
                    Result.success(SavedAttachment(displayPath(folder, name)))
                } catch (e: Exception) {
                    context.contentResolver.delete(uri, null, null)
                    throw e
                }
            } else {
                val dir = publicFolderDir(folder)
                val name = uniqueFileName(dir, baseName)
                val dest = File(dir, name)
                source.inputStream().use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                Result.success(SavedAttachment(displayPath(folder, name), dest))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveLocalFile failed", e)
            Result.failure(e)
        }
    }

    /** 公共下载目录下的媒体文件夹 */
    private fun publicFolderDir(folder: String): File {
        @Suppress("DEPRECATION")
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dir = File(File(downloads, ROOT_FOLDER), folder)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** 供提示展示的相对路径，如 Download/VoceChat/Images/photo.jpg */
    private fun displayPath(folder: String, name: String): String =
        "${Environment.DIRECTORY_DOWNLOADS}/$ROOT_FOLDER/$folder/$name"

    /** 清洗文件名并补齐扩展名 */
    private fun fileNameFor(rawName: String, mimeType: String): String {
        val cleaned = rawName.replace(ILLEGAL_CHARS, "_").trim().trim('.').ifBlank { "attachment" }
        val dot = cleaned.lastIndexOf('.')
        val stem = (if (dot > 0) cleaned.substring(0, dot) else cleaned)
            .take(100)
            .ifBlank { "attachment" }
        val ext = if (dot > 0) cleaned.substring(dot) else extensionForMime(mimeType)?.let { ".$it" } ?: ""
        return "$stem$ext"
    }

    /** 同名文件追加序号 */
    private fun withIndex(name: String, index: Int): String {
        val dot = name.lastIndexOf('.')
        return if (dot > 0) {
            "${name.substring(0, dot)} ($index)${name.substring(dot)}"
        } else {
            "$name ($index)"
        }
    }

    /** 目录中不存在重名文件时返回可用文件名 */
    private fun uniqueFileName(dir: File, baseName: String): String {
        var candidate = baseName
        var index = 1
        while (File(dir, candidate).exists()) {
            candidate = withIndex(baseName, index++)
        }
        return candidate
    }

    /** MediaStore 中不存在重名文件时返回可用文件名 */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun uniqueMediaStoreName(context: Context, baseName: String, relativePath: String): String {
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection =
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val args = arrayOf(baseName, "$relativePath/")
        var candidate = baseName
        var index = 1
        while (true) {
            val exists = resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                args,
                null
            )?.use { cursor -> cursor.count > 0 } == true
            if (!exists) return candidate
            candidate = withIndex(baseName, index++)
        }
    }

    /** 在共享下载目录中创建待写入的条目 */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertPending(
        context: Context,
        name: String,
        mimeType: String,
        relativePath: String
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        return context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Cannot create $name in $relativePath")
    }

    /** 标记写入完成，文件此时对其他应用可见 */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun publishPending(context: Context, uri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        context.contentResolver.update(uri, values, null, null)
    }

    /** 根据扩展名推断 MIME 类型 */
    fun mimeTypeFromName(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "heic", "heif" -> "image/heic"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "3gp" -> "video/3gpp"
            "mkv" -> "video/x-matroska"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "opus" -> "audio/opus"
            "wav" -> "audio/wav"
            "amr" -> "audio/amr"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    /** 根据 MIME 类型推断扩展名 */
    private fun extensionForMime(mimeType: String): String? = when (mimeType.lowercase()) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        "image/bmp" -> "bmp"
        "image/heic" -> "heic"
        "video/mp4" -> "mp4"
        "video/webm" -> "webm"
        "video/quicktime" -> "mov"
        "video/3gpp" -> "3gp"
        "video/x-matroska" -> "mkv"
        "audio/mpeg" -> "mp3"
        "audio/mp4" -> "m4a"
        "audio/aac" -> "aac"
        "audio/ogg" -> "ogg"
        "audio/opus" -> "opus"
        "audio/wav" -> "wav"
        "audio/amr" -> "amr"
        "application/pdf" -> "pdf"
        else -> null
    }
}
