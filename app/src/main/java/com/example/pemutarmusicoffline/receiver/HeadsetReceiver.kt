package com.example.pemutarmusicoffline.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import com.example.pemutarmusicoffline.MusicService

class HeadsetReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            // Headset dilepas, pause musik
            val pauseIntent = Intent(context, MusicService::class.java).apply {
                action = MusicService.ACTION_PAUSE
            }
            context.startService(pauseIntent)
        }
    }
    
    companion object {
        fun register(context: Context): HeadsetReceiver {
            val receiver = HeadsetReceiver()
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            context.registerReceiver(receiver, filter)
            return receiver
        }
    }
}
