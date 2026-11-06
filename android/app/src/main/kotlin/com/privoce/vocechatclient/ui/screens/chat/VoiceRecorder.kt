package com.privoce.vocechatclient.ui.screens.chat

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

data class VoiceRecordResult(
    val file: File,
    val durationMs: Long
)

/**
 * Simple MediaRecorder wrapper producing m4a (AAC) files.
 */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Long = 0L

    val isActive: Boolean
        get() = recorder != null

    fun start(): Boolean {
        if (recorder != null) return false

        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        val mediaRecorder = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to create MediaRecorder", e)
            return false
        }

        return try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioEncodingBitRate(64_000)
            mediaRecorder.setAudioSamplingRate(44_100)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()

            recorder = mediaRecorder
            outputFile = file
            startedAt = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to start recording", e)
            try {
                mediaRecorder.release()
            } catch (_: Exception) {
            }
            file.delete()
            false
        }
    }

    fun stop(): VoiceRecordResult? {
        val mediaRecorder = recorder ?: return null
        val file = outputFile
        val duration = System.currentTimeMillis() - startedAt
        recorder = null
        outputFile = null

        return try {
            mediaRecorder.stop()
            if (file == null || !file.exists() || file.length() == 0L) {
                file?.delete()
                null
            } else {
                VoiceRecordResult(file, duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to stop recording", e)
            file?.delete()
            null
        } finally {
            try {
                mediaRecorder.release()
            } catch (_: Exception) {
            }
        }
    }

    fun cancel() {
        val mediaRecorder = recorder ?: return
        recorder = null
        outputFile?.delete()
        outputFile = null
        try {
            mediaRecorder.stop()
        } catch (_: Exception) {
        }
        try {
            mediaRecorder.release()
        } catch (_: Exception) {
        }
    }

    private companion object {
        const val TAG = "VoiceRecorder"
    }
}
