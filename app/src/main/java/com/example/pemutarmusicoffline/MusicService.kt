package com.example.pemutarmusicoffline

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.pemutarmusicoffline.notification.MediaNotificationManager
import com.example.pemutarmusicoffline.receiver.AudioFocusHelper
import com.example.pemutarmusicoffline.receiver.HeadsetReceiver

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSongUri: Uri? = null
    private var currentSong: Song? = null
    private var isPlaying = false
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: MediaNotificationManager
    private lateinit var audioFocusHelper: AudioFocusHelper
    private var headsetReceiver: HeadsetReceiver? = null
    
    private val binder = MusicBinder()
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            updateNotification()
        }
        
        // Inisialisasi MediaSession
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.isActive = true
        
        // Inisialisasi NotificationManager
        notificationManager = MediaNotificationManager(this)
        
        // Inisialisasi AudioFocusHelper
        audioFocusHelper = AudioFocusHelper(this, this)
        
        // Register HeadsetReceiver
        headsetReceiver = HeadsetReceiver.register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val songUriString = intent.getStringExtra(EXTRA_SONG_URI)
                val songTitle = intent.getStringExtra(EXTRA_SONG_TITLE)
                val songArtist = intent.getStringExtra(EXTRA_SONG_ARTIST)
                val songId = intent.getLongExtra(EXTRA_SONG_ID, -1)
                
                if (songUriString != null && songTitle != null && songArtist != null && songId != -1L) {
                    val songUri = Uri.parse(songUriString)
                    currentSong = Song(songId, songTitle, songArtist, songUriString)
                    playMusic(songUri)
                }
            }
            ACTION_PAUSE -> pauseMusic()
            ACTION_STOP -> stopMusic()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundCompat(true)
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
        audioFocusHelper.abandonAudioFocus()
        headsetReceiver?.let { unregisterReceiver(it) }
    }

    fun playMusic(uri: Uri) {
        if (audioFocusHelper.requestAudioFocus()) {
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(this, uri)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                currentSongUri = uri
                isPlaying = true
                
                // Mulai foreground service dengan notifikasi
                startForegroundWithNotification()
            } catch (e: Exception) {
                Log.e("MusicService", "Error playing music", e)
            }
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
        updateNotification()
    }

    fun resumeMusic() {
        if (audioFocusHelper.requestAudioFocus()) {
            mediaPlayer?.start()
            isPlaying = true
            updateNotification()
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        isPlaying = false
        currentSong = null
        stopForegroundCompat(true)
    }
    
    fun getCurrentSong(): Song? {
        return currentSong
    }
    
    fun isPlaying(): Boolean {
        return isPlaying
    }
    
    private fun startForegroundWithNotification() {
        val notification = notificationManager.buildNotification(currentSong, isPlaying, mediaSession)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun updateNotification() {
        notificationManager.updateNotification(currentSong, isPlaying, mediaSession)
    }

    /**
     * Metode helper untuk menghentikan foreground service dengan cara yang kompatibel
     * dengan berbagai versi Android
     */
    private fun stopForegroundCompat(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(if (removeNotification) Service.STOP_FOREGROUND_REMOVE else Service.STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(removeNotification)
        }
    }
    
    companion object {
        const val ACTION_PLAY = "com.example.pemutarmusicoffline.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.pemutarmusicoffline.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.pemutarmusicoffline.ACTION_STOP"
        const val EXTRA_SONG_URI = "com.example.pemutarmusicoffline.EXTRA_SONG_URI"
        const val EXTRA_SONG_TITLE = "com.example.pemutarmusicoffline.EXTRA_SONG_TITLE"
        const val EXTRA_SONG_ARTIST = "com.example.pemutarmusicoffline.EXTRA_SONG_ARTIST"
        const val EXTRA_SONG_ID = "com.example.pemutarmusicoffline.EXTRA_SONG_ID"
        private const val NOTIFICATION_ID = 1
    }
}
