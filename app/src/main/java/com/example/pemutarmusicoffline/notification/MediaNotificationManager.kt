package com.example.pemutarmusicoffline.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.pemutarmusicoffline.MainActivity
import com.example.pemutarmusicoffline.MusicService
import com.example.pemutarmusicoffline.R
import com.example.pemutarmusicoffline.Song

class MediaNotificationManager(private val context: Context) {
    
    private val notificationManager: NotificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val channelId = "music_player_channel"
    private val notificationId = 1
    
    init {
        // Buat channel notifikasi untuk Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for the music player"
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(song: Song?, isPlaying: Boolean, mediaSession: MediaSessionCompat): Notification {
        // Intent untuk membuka MainActivity saat notifikasi diklik
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent untuk aksi play/pause
        val playPauseIntent = Intent(context, MusicService::class.java).apply {
            action = if (isPlaying) MusicService.ACTION_PAUSE else MusicService.ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            context,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent untuk aksi stop
        val stopIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Buat notifikasi
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_music_note) 
            .setContentTitle(song?.title ?: "No song playing")
            .setContentText(song?.artist ?: "")
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
        
        // Tambahkan aksi play/pause
        builder.addAction(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "Pause" else "Play",
            playPausePendingIntent
        )
        
        // Tambahkan aksi stop
        builder.addAction(
            R.drawable.ic_stop,
            "Stop",
            stopPendingIntent
        )
        
        // Set style media
        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1) // Indeks aksi yang ditampilkan dalam tampilan compact
        
        builder.setStyle(mediaStyle)
        
        return builder.build()
    }
    
    fun updateNotification(song: Song?, isPlaying: Boolean, mediaSession: MediaSessionCompat) {
        val notification = buildNotification(song, isPlaying, mediaSession)
        notificationManager.notify(notificationId, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }
}
