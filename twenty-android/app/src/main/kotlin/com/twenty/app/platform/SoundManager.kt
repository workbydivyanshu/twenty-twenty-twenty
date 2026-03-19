package com.twenty.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    private fun playTone(freq: Float, duration: Float, volume: Float = 0.7f) {
        scope.launch {
            val numSamples = (duration * sampleRate).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val angle = 2.0 * PI * i * freq / sampleRate
                val envelope = exp(-3.0 * i / numSamples)
                samples[i] = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt().toShort()
            }

            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()

                Thread.sleep((duration * 1000 + 100).toLong())
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }

    fun playBreakChime(volume: Float = 0.7f) {
        scope.launch {
            playTone(528f, 0.15f, volume * 0.5f)
            Thread.sleep(50)
            playTone(660f, 0.15f, volume * 0.4f)
        }
    }

    fun playConfirm(volume: Float = 0.7f) {
        scope.launch {
            playTone(660f, 0.15f, volume * 0.5f)
            Thread.sleep(100)
            playTone(880f, 0.2f, volume * 0.5f)
        }
    }

    fun playStart(volume: Float = 0.7f) {
        scope.launch {
            playTone(523f, 0.15f, volume * 0.4f)
            Thread.sleep(120)
            playTone(659f, 0.15f, volume * 0.4f)
            Thread.sleep(120)
            playTone(784f, 0.2f, volume * 0.4f)
        }
    }

    fun playEnd(volume: Float = 0.7f) {
        scope.launch {
            playTone(784f, 0.15f, volume * 0.4f)
            Thread.sleep(120)
            playTone(659f, 0.15f, volume * 0.4f)
            Thread.sleep(120)
            playTone(523f, 0.25f, volume * 0.4f)
        }
    }

    fun playSkip(volume: Float = 0.7f) {
        playTone(220f, 0.1f, volume * 0.3f)
    }
}
