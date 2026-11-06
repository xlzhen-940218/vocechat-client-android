package com.privoce.vocechatclient

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream

class MainActivity : FlutterFragmentActivity() {
    private val mediaStoreChannel = "vocechat/media_store"
    private val videoThumbnailChannel = "vocechat/video_thumbnail"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, mediaStoreChannel)
            .setMethodCallHandler { call, result ->
                if (call.method != "saveMedia") {
                    result.notImplemented()
                    return@setMethodCallHandler
                }

                val path = call.argument<String>("path")
                val fileName = call.argument<String>("fileName")
                val mimeType = call.argument<String>("mimeType")
                val mediaType = call.argument<String>("mediaType") ?: "downloads"

                if (path.isNullOrEmpty() || fileName.isNullOrEmpty()) {
                    result.error("INVALID_ARGUMENTS", "path and fileName are required", null)
                    return@setMethodCallHandler
                }

                // Copying large videos must not block the platform thread.
                Thread {
                    try {
                        val savedUri = saveMedia(File(path), fileName, mimeType, mediaType)
                        runOnUiThread { result.success(savedUri?.toString()) }
                    } catch (e: Exception) {
                        runOnUiThread {
                            result.error("SAVE_FAILED", e.message ?: "Failed to save media", null)
                        }
                    }
                }.start()
            }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, videoThumbnailChannel)
            .setMethodCallHandler { call, result ->
                if (call.method != "generateThumbnail") {
                    result.notImplemented()
                    return@setMethodCallHandler
                }

                val videoUrl = call.argument<String>("videoUrl")
                val targetPath = call.argument<String>("path")
                val maxHeight = call.argument<Int>("maxHeight") ?: 64
                val quality = call.argument<Int>("quality") ?: 75

                if (videoUrl.isNullOrEmpty() || targetPath.isNullOrEmpty()) {
                    result.error("INVALID_ARGUMENTS", "videoUrl and path are required", null)
                    return@setMethodCallHandler
                }

                Thread {
                    try {
                        val thumbnail = generateVideoThumbnail(videoUrl, targetPath, maxHeight, quality)
                        runOnUiThread { result.success(thumbnail) }
                    } catch (e: Exception) {
                        runOnUiThread {
                            result.error("THUMBNAIL_FAILED", e.message ?: "Failed to create thumbnail", null)
                        }
                    }
                }.start()
            }
    }

    /// Extracts the first frame of a video. MediaMetadataRetriever replaces the
    /// discontinued `video_thumbnail` Flutter plugin.
    private fun generateVideoThumbnail(
        videoUrl: String,
        targetPath: String,
        maxHeight: Int,
        quality: Int
    ): String? {
        val retriever = MediaMetadataRetriever()
        try {
            if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
                retriever.setDataSource(videoUrl, HashMap<String, String>())
            } else {
                retriever.setDataSource(videoUrl)
            }

            val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: return null

            val bitmap = if (frame.height > maxHeight && frame.height > 0) {
                val ratio = maxHeight.toFloat() / frame.height.toFloat()
                val width = (frame.width * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(frame, width, maxHeight, true)
            } else {
                frame
            }

            File(targetPath).parentFile?.mkdirs()
            FileOutputStream(targetPath).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            }

            if (bitmap !== frame) {
                bitmap.recycle()
            }
            frame.recycle()

            return targetPath
        } finally {
            retriever.release()
        }
    }

    private fun saveMedia(
        file: File,
        fileName: String,
        mimeType: String?,
        mediaType: String
    ): Uri? {
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: ${file.path}")
        }

        val relativeDir = when (mediaType) {
            "image" -> Environment.DIRECTORY_PICTURES
            "video" -> Environment.DIRECTORY_MOVIES
            "audio" -> Environment.DIRECTORY_MUSIC
            else -> Environment.DIRECTORY_DOWNLOADS
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = when (mediaType) {
                "image" -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                "video" -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                "audio" -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                if (!mimeType.isNullOrEmpty()) {
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                }
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$relativeDir/VoceChat")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(collection, values)
                ?: throw IllegalStateException("Failed to create a MediaStore entry")

            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input -> input.copyTo(output) }
                } ?: throw IllegalStateException("Failed to open an output stream")

                val pending = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                contentResolver.update(uri, pending, null, null)
                uri
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
                throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val publicDir = Environment.getExternalStoragePublicDirectory(relativeDir)
            val targetDir = File(publicDir, "VoceChat")
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IllegalStateException("Failed to create directory: ${targetDir.path}")
            }
            val target = File(targetDir, fileName)
            file.copyTo(target, overwrite = true)
            Uri.fromFile(target)
        }
    }

    override fun onResume() {
        super.onResume()
        closeAllNotifications()
    }

    private fun closeAllNotifications() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
