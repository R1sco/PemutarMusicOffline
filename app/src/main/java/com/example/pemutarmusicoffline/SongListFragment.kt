package com.example.pemutarmusicoffline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pemutarmusicoffline.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongListFragment : Fragment() {

    interface MusicControlListener {
        fun onPlaySong(song: Song)
        fun onPauseSong()
        fun onStopSong()
    }

    interface FavoriteToggleListener {
        suspend fun toggleFavorite(song: Song, isFavorite: Boolean)
    }

    private var musicControlListener: MusicControlListener? = null
    private var favoriteToggleListener: FavoriteToggleListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MusicControlListener) {
            musicControlListener = context
        } else {
            throw RuntimeException("$context harus mengimplementasikan MusicControlListener")
        }
        
        if (context is FavoriteToggleListener) {
            favoriteToggleListener = context
        } else {
            throw RuntimeException("$context harus mengimplementasikan FavoriteToggleListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SongListScreen()
                }
            }
        }
    }

    @Composable
    fun SongListScreen() {
        val context = LocalContext.current
        var songs by rememberSaveable { mutableStateOf(listOf<Song>()) }
        var currentSong by rememberSaveable { mutableStateOf<Song?>(null) }
        var isPlaying by rememberSaveable { mutableStateOf(false) }
        var favoriteSongIds by remember { mutableStateOf(setOf<Long>()) }
        
        // Memuat daftar lagu
        LaunchedEffect(key1 = Unit) {
            withContext(Dispatchers.IO) {
                songs = loadSongs(context)
                // Memuat daftar ID lagu favorit
                val dao = AppDatabase.getDatabase(context).favoriteSongDao()
                val favoritesFlow = dao.getAllFavoriteSongs()
                favoritesFlow.collect { listOfFavorites ->
                    favoriteSongIds = listOfFavorites.map { it.songId }.toSet()
                }
            }
        }
        
        // Menggunakan derivedStateOf untuk kalkulasi yang bergantung pada state lain
        val sortedSongs by remember(songs) {
            derivedStateOf { songs.sortedBy { it.title } }
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Daftar lagu
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = sortedSongs,
                    key = { it.id }
                ) { song ->
                    SongItem(
                        song = song,
                        isSelected = song.id == currentSong?.id,
                        isFavorite = favoriteSongIds.contains(song.id),
                        onClick = {
                            if (currentSong?.id == song.id && isPlaying) {
                                // Jika lagu yang sama dan sedang diputar, pause
                                musicControlListener?.onPauseSong()
                                isPlaying = false
                            } else {
                                // Jika lagu berbeda atau tidak diputar, putar
                                currentSong = song
                                musicControlListener?.onPlaySong(song)
                                isPlaying = true
                            }
                        },
                        onFavoriteToggle = { isFavorite ->
                            // Toggle favorit status
                            lifecycleScope.launch {
                                favoriteToggleListener?.toggleFavorite(song, isFavorite)
                                // Update daftar favorit
                                withContext(Dispatchers.IO) {
                                    val dao = AppDatabase.getDatabase(context).favoriteSongDao()
                                    val favoritesFlow = dao.getAllFavoriteSongs()
                                    favoritesFlow.collect { listOfFavorites ->
                                        favoriteSongIds = listOfFavorites.map { it.songId }.toSet()
                                    }
                                }
                            }
                        }
                    )
                }
            }
            
            // Kontrol pemutaran
            currentSong?.let { song ->
                PlayerControls(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayClick = {
                        musicControlListener?.onPlaySong(song)
                        isPlaying = true
                    },
                    onPauseClick = {
                        musicControlListener?.onPauseSong()
                        isPlaying = false
                    },
                    onStopClick = {
                        musicControlListener?.onStopSong()
                        isPlaying = false
                        currentSong = null
                    }
                )
            }
        }
    }

    @Composable
    fun SongItem(
        song: Song,
        isSelected: Boolean,
        isFavorite: Boolean,
        onClick: () -> Unit,
        onFavoriteToggle: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 1.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon untuk musik
                Icon(
                    painter = painterResource(id = R.drawable.ic_music_note),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Icon favorit
                IconButton(onClick = { onFavoriteToggle(!isFavorite) }) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                        ),
                        contentDescription = if (isFavorite) "Hapus dari favorit" else "Tambahkan ke favorit",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun PlayerControls(
        song: Song,
        isPlaying: Boolean,
        onPlayClick: () -> Unit,
        onPauseClick: () -> Unit,
        onStopClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indikator status pemutaran
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying) "Now Playing" else "Paused",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    // Tombol play
                    IconButton(onClick = onPlayClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Tombol pause
                    IconButton(onClick = onPauseClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "Pause",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Tombol stop
                    IconButton(onClick = onStopClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop),
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
    
    private fun loadSongs(context: Context): List<Song> {
        return com.example.pemutarmusicoffline.loadSongs(context)
    }
    
    override fun onDetach() {
        super.onDetach()
        musicControlListener = null
        favoriteToggleListener = null
    }
}
