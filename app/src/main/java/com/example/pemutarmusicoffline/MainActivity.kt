package com.example.pemutarmusicoffline

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pemutarmusicoffline.database.AppDatabase
import com.example.pemutarmusicoffline.database.FavoriteSong
import com.example.pemutarmusicoffline.fragment.FavoriteFragment
import com.example.pemutarmusicoffline.fragment.SongListFragment
import com.example.pemutarmusicoffline.ui.theme.PemutarMusicOfflineTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity(), FavoriteFragment.MusicControlListener, 
                     SongListFragment.MusicControlListener, SongListFragment.FavoriteToggleListener {
    
    private var musicService: MusicService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Menggunakan pendekatan modern untuk izin dengan registerForActivityResult
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Izin diberikan, lanjutkan dengan operasi normal
                bindMusicService()
            } else {
                // Izin ditolak, tampilkan pesan atau alternatif
                Toast.makeText(
                    this,
                    "Aplikasi memerlukan izin penyimpanan untuk menampilkan lagu",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        // Request appropriate storage permission based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+), use READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                bindMusicService()
            }
        } else {
            // For older versions, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                bindMusicService()
            }
        }
        
        setContent {
            PemutarMusicOfflineTheme {
                MusicPlayerApp()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MusicPlayerApp() {
        val navController = rememberNavController()
        var selectedTab by remember { mutableStateOf(0) }
        
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { 
                            selectedTab = 0
                            navController.navigate("songList") {
                                popUpTo("songList") { inclusive = true }
                            }
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_music_note), contentDescription = "Daftar Lagu") },
                        label = { Text("Lagu") }
                    )
                    
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { 
                            selectedTab = 1 
                            navController.navigate("favorites") {
                                popUpTo("favorites") { inclusive = true }
                            }
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_favorite), contentDescription = "Lagu Favorit") },
                        label = { Text("Favorit") }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "songList",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("songList") {
                    // Tampilkan SongListFragment
                    val fragment = SongListFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit()
                }
                
                composable("favorites") {
                    // Tampilkan FavoriteFragment
                    val fragment = FavoriteFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit()
                }
            }
        }
    }
    
    private fun bindMusicService() {
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    override fun onStart() {
        super.onStart()
        if (!isBound) {
            bindMusicService()
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
    
    // Implementasi MusicControlListener
    override fun onPlaySong(song: Song) {
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY
            putExtra(MusicService.EXTRA_SONG_URI, song.filePath)
            putExtra(MusicService.EXTRA_SONG_TITLE, song.title)
            putExtra(MusicService.EXTRA_SONG_ARTIST, song.artist)
            putExtra(MusicService.EXTRA_SONG_ID, song.id)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    override fun onPauseSong() {
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_PAUSE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    override fun onStopSong() {
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    override fun onSeek(positionMs: Int) {
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_POSITION_MS, positionMs)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    // Implementasi FavoriteToggleListener
    override suspend fun toggleFavorite(song: Song, isFavorite: Boolean) {
        val dao = AppDatabase.getDatabase(this).favoriteSongDao()
        
        if (isFavorite) {
            // Tambahkan ke favorit
            val favoriteSong = FavoriteSong(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                filePath = song.filePath
            )
            dao.insertFavoriteSong(favoriteSong)
        } else {
            // Hapus dari favorit
            dao.deleteFavoriteSongById(song.id)
        }
    }
}
