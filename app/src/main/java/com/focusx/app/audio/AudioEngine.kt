package com.focusx.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.*

class AudioEngine(private val context: Context) {
    private var sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var scope: CoroutineScope? = null
    private var ambientJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playChime(type: String, enabled: Boolean) {
        if (!enabled) return
        when (type) {
            "start" -> playToneSequence(doubleArrayOf(523.25, 659.25, 783.99), intArrayOf(100, 100, 100), 80)
            "pause" -> playTone(440.0, 120, 60)
            "complete" -> {
                playToneSequence(doubleArrayOf(523.25, 659.25, 783.99), intArrayOf(150, 150, 150), 60)
                playToneSequence(doubleArrayOf(587.33, 739.99, 880.0), intArrayOf(150, 150, 150), 60)
            }
        }
    }

    fun haptic(pattern: String, enabled: Boolean) {
        if (!enabled || vibrator == null) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (pattern) {
                "start" -> VibrationEffect.createWaveform(longArrayOf(0, 15, 30, 15), intArrayOf(0, 255, 0, 255), -1)
                "pause" -> VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                "complete" -> VibrationEffect.createWaveform(longArrayOf(0, 20, 50, 20, 50, 20), intArrayOf(0, 255, 0, 255, 0, 255), -1)
                "tap" -> VibrationEffect.createOneShot(6, VibrationEffect.DEFAULT_AMPLITUDE)
                else -> return
            }
            v.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(when (pattern) {
                "start" -> longArrayOf(0, 15, 30, 15)
                "pause" -> longArrayOf(10)
                "complete" -> longArrayOf(0, 20, 50, 20, 50, 20)
                "tap" -> longArrayOf(6)
                else -> return
            }, -1)
        }
    }

    fun startAmbient(mode: String, enabled: Boolean) {
        stopAmbient()
        if (!enabled || mode == "none") return
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ambientJob = scope?.launch {
            val duration = 2 * sampleRate
            val buffer = when (mode) {
                "rain" -> generateRain(duration)
                "whitenoise" -> generateWhiteNoise(duration)
                "cafe" -> generateCafe(duration)
                else -> return@launch
            }
            val track = createAudioTrack()
            if (track == null) { stopAmbient(); return@launch }
            audioTrack = track
            try {
                track.play()
                var offset = 0
                while (isActive && offset < buffer.size) {
                    val chunk = minOf(buffer.size - offset, sampleRate)
                    track.write(buffer, offset, chunk)
                    offset += chunk
                    if (offset >= buffer.size) offset = 0
                }
            } catch (_: Exception) {} finally {
                try { track.stop(); track.release() } catch (_: Exception) {}
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        scope?.cancel()
        scope = null
        try { audioTrack?.stop(); audioTrack?.release() } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stopAmbient()
    }

    private fun createAudioTrack(): AudioTrack? {
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate,
            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        return try {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) { null }
    }

    private fun playTone(freq: Double, durationMs: Int, volume: Int) {
        try {
            val totalSamples = sampleRate * durationMs / 1000
            val buffer = ShortArray(totalSamples)
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = if (i < totalSamples / 8) i.toDouble() / (totalSamples / 8)
                    else (totalSamples - i.toDouble()) / (totalSamples * 7 / 8)
                buffer[i] = (Math.sin(2.0 * Math.PI * freq * t) * volume * envelope).toInt().toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buffer, 0, buffer.size)
            track.play()
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }

    private fun playToneSequence(freqs: DoubleArray, durationsMs: IntArray, volume: Int) {
        try {
            val totalSamples = freqs.indices.sumOf { sampleRate * durationsMs[it] / 1000 }
            val buffer = ShortArray(totalSamples)
            var pos = 0
            freqs.indices.forEach { i ->
                val count = sampleRate * durationsMs[i] / 1000
                for (j in 0 until count) {
                    val t = j.toDouble() / sampleRate
                    val envelope = if (j < count / 6) j.toDouble() / (count / 6)
                        else (count - j.toDouble()) / (count * 5 / 6)
                    buffer[pos++] = (Math.sin(2.0 * Math.PI * freqs[i] * t) * volume * envelope).toInt().toShort()
                }
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buffer, 0, buffer.size)
            track.play()
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }

    private fun generateWhiteNoise(samples: Int): ShortArray {
        val buf = ShortArray(samples)
        for (i in buf.indices) buf[i] = (Math.random() * 2 - 1).toInt().toShort()
        return buf
    }

    private fun generateRain(samples: Int): ShortArray {
        val buf = ShortArray(samples)
        for (i in buf.indices) {
            val noise = (Math.random() * 2 - 1) * Math.pow(Math.random(), 4.0)
            buf[i] = (noise * 3000).toInt().toShort()
        }
        applyLowPass(buf, 0.15)
        return buf
    }

    private fun generateCafe(samples: Int): ShortArray {
        val buf = ShortArray(samples)
        for (i in buf.indices) {
            val t = i.toDouble() / sampleRate
            val murmur = (Math.sin(t * 170.0) * 0.3 + Math.sin(t * 230.0) * 0.2 + Math.sin(t * 310.0) * 0.15)
            val noise = (Math.random() * 2 - 1) * 0.4
            val envelope = 0.5 + 0.5 * Math.sin(t * 1.5)
            buf[i] = ((murmur + noise) * envelope * 2000).toInt().toShort()
        }
        return buf
    }

    private fun applyLowPass(buf: ShortArray, alpha: Double) {
        var prev = 0.0
        for (i in buf.indices) {
            prev = alpha * prev + (1 - alpha) * buf[i]
            buf[i] = prev.toInt().toShort()
        }
    }
}
