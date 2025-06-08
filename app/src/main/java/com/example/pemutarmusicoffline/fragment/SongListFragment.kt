package com.example.pemutarmusicoffline.fragment

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pemutarmusicoffline.R
import com.example.pemutarmusicoffline.Song
import com.example.pemutarmusicoffline.database.AppDatabase
import com.example.pemutarmusicoffline.database.FavoriteSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongListFragment : Fragment() {
    
    private var musicControlListener: MusicControlListener? = null
    private var favoriteToggleListener: FavoriteToggleListener? = null
    
    interface MusicControlListener {
        fun onPlaySong(song: Song)
        fun onPauseSong()
        fun onStopSong()
    }
    
    interface FavoriteToggleListener {
        suspend fun toggleFavorite(song: Song, isFavorite: Boolean)
    }
    
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
        val view = inflater.inflate(R.layout.fragment_music_player, container, false)
        val composeContainer = view.findViewById<ViewGroup>(R.id.compose_container)
        
        // Tambahkan ComposeView secara programatis
        val composeView = ComposeView(requireContext())
        composeContainer.addView(composeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        
        composeView.setContent {
            MaterialTheme {
                SongListScreen()
            }
        }
        
        return view
    }
    
    @Composable
    fun SongListScreen() {
        val context = requireContext()
        var songs by rememberSaveable { mutableStateOf(listOf<Song>()) }
        var currentSong by rememberSaveable { mutableStateOf<Song?>(null) }
        var isPlaying by rememberSaveable { mutableStateOf(false) }
        var favoriteSongIds by remember { mutableStateOf(setOf<Long>()) }
        
        // Memuat daftar lagu
        LaunchedEffect(key1 = Unit) {
            withContext(Dispatchers.IO) {
                songs = loadSongs(context)
            }
        }
        
        // Memuat dan mengamati daftar lagu favorit
        val dao = remember { AppDatabase.getDatabase(context).favoriteSongDao() }
        LaunchedEffect(key1 = Unit) {
            dao.getAllFavoriteSongs().collect { favoriteSongs ->
                favoriteSongIds = favoriteSongs.map { it.songId }.toSet()
            }
        }
        
        val sortedSongs by remember(songs) {
            derivedStateOf { songs.sortedBy { it.title } }
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Semua Lagu",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items = sortedSongs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isSelected = song.id == currentSong?.id,
                        isFavorite = favoriteSongIds.contains(song.id),
                        onClick = {
                            if (currentSong?.id == song.id && isPlaying) {
                                musicControlListener?.onPauseSong()
                                isPlaying = false
                            } else {
                                currentSong = song
                                musicControlListener?.onPlaySong(song)
                                isPlaying = true
                            }
                        },
                        onFavoriteToggle = { isFavorite ->
                            lifecycleScope.launch {
                                favoriteToggleListener?.toggleFavorite(song, isFavorite)
                            }
                        }
                    )
                }
            }
            
            PlayerControls(
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayClick = {
                    currentSong?.let {
                        musicControlListener?.onPlaySong(it)
                        isPlaying = true
                    }
                },
                onPauseClick = {
                    musicControlListener?.onPauseSong()
                    isPlaying = false
                },
                onStopClick = {
                    musicControlListener?.onStopSong()
                    isPlaying = false
                }
            )
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
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_music_note),
                    contentDescription = "Music Icon",
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = { onFavoriteToggle(!isFavorite) }) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                        ),
                        contentDescription = if (isFavorite) "Hapus dari favorit" else "Tambahkan ke favorit"
                    )
                }
            }
        }
    }
    
    @Composable
    fun PlayerControls(
        currentSong: Song?,
        isPlaying: Boolean,
        onPlayClick: () -> Unit,
        onPauseClick: () -> Unit,
        onStopClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Song info
                if (currentSong != null) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Tidak ada lagu yang dipilih",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onPlayClick,
                        enabled = currentSong != null && !isPlaying
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Putar",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onPauseClick,
                        enabled = currentSong != null && isPlaying
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "Jeda",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onStopClick,
                        enabled = currentSong != null
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop),
                            contentDescription = "Berhenti",
                            modifier = Modifier.size(32.dp)
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
