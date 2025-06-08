package com.example.pemutarmusicoffline

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pemutarmusicoffline.loadSongs
import com.example.pemutarmusicoffline.playSong
import com.example.pemutarmusicoffline.pauseSong
import com.example.pemutarmusicoffline.stopSong

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    // Menggunakan rememberSaveable untuk mempertahankan state saat konfigurasi berubah
    var songs by rememberSaveable { mutableStateOf(listOf<Song>()) }
    var currentSong by rememberSaveable { mutableStateOf<Song?>(null) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    
    // Menggunakan key yang lebih spesifik untuk LaunchedEffect
    LaunchedEffect(key1 = context) {
        songs = loadSongs(context)
    }
    
    // Menggunakan derivedStateOf untuk kalkulasi yang bergantung pada state lain
    val sortedSongs by remember(songs) {
        derivedStateOf { songs.sortedBy { it.title } }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Song list dengan padding yang lebih baik
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = sortedSongs,
                key = { it.id } // Menggunakan key untuk performa yang lebih baik
            ) { song ->
                SongItem(
                    song = song, 
                    isSelected = song.id == currentSong?.id,
                    onClick = {
                        if (currentSong?.id == song.id && isPlaying) {
                            // Jika lagu yang sama dan sedang diputar, pause
                            pauseSong(context)
                            isPlaying = false
                        } else {
                            // Jika lagu berbeda atau tidak diputar, putar
                            currentSong = song
                            playSong(context, song)
                            isPlaying = true
                        }
                    }
                )
            }
        }
        
        // Player controls dengan state isPlaying
        PlayerControls(
            currentSong = currentSong,
            isPlaying = isPlaying,
            onPlayClick = { 
                currentSong?.let { 
                    playSong(context, it) 
                    isPlaying = true
                } 
            },
            onPauseClick = { 
                pauseSong(context) 
                isPlaying = false
            },
            onStopClick = { 
                stopSong(context)
                isPlaying = false
                currentSong = null
            }
        )
    }
}

@Composable
fun SongItem(song: Song, isSelected: Boolean, onClick: () -> Unit) {
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
                imageVector = Icons.Filled.PlayArrow,
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            currentSong?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indikator status pemutaran
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Now Playing" else "Paused",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = it.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = it.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            } ?: run {
                // Tampilkan pesan jika tidak ada lagu yang dipilih
                Text(
                    text = "Pilih lagu untuk diputar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Play dengan ikon
                FilledTonalIconButton(
                    onClick = onPlayClick,
                    enabled = currentSong != null
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play"
                    )
                }
                
                // Tombol Pause dengan ikon
                FilledTonalIconButton(
                    onClick = onPauseClick,
                    enabled = currentSong != null && isPlaying
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause"
                    )
                }
                
                // Tombol Stop dengan ikon
                FilledTonalIconButton(
                    onClick = onStopClick,
                    enabled = currentSong != null
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop"
                    )
                }
            }
        }
    }
}


