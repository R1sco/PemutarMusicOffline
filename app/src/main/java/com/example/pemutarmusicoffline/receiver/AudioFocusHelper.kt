package com.example.pemutarmusicoffline.receiver

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.example.pemutarmusicoffline.MusicService

class AudioFocusHelper(private val context: Context, private val musicService: MusicService) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var playbackDelayed = false
    private var resumeOnFocusGain = false
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Kehilangan fokus untuk waktu yang lama, hentikan pemutaran
                musicService.pauseMusic()
                resumeOnFocusGain = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Kehilangan fokus sementara, pause pemutaran
                musicService.pauseMusic()
                resumeOnFocusGain = true
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Kehilangan fokus sementara, tapi bisa menurunkan volume
                // Implementasi duck volume jika diperlukan
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Mendapatkan fokus kembali
                if (resumeOnFocusGain) {
                    musicService.resumeMusic()
                    resumeOnFocusGain = false
                }
            }
        }
    }
    
    fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
}
