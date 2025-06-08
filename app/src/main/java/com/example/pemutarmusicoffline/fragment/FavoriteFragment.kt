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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
    
    private var musicControlListener: MusicControlListener? = null
    
    interface MusicControlListener {
        fun onPlaySong(song: Song)
        fun onPauseSong()
        fun onStopSong()
    }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MusicControlListener) {
            musicControlListener = context
        } else {
            throw RuntimeException("$context must implement MusicControlListener")
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
                FavoriteScreen()
            }
        }
        
        return view
    }
    
    @Composable
    fun FavoriteScreen() {
        val context = requireContext()
        var favoriteSongs by remember { mutableStateOf<List<FavoriteSong>>(emptyList()) }
        
        val dao = remember { AppDatabase.getDatabase(context).favoriteSongDao() }
        
        LaunchedEffect(key1 = Unit) {
            dao.getAllFavoriteSongs().collectLatest { songs ->
                favoriteSongs = songs
            }
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Lagu Favorit",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            if (favoriteSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada lagu favorit")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(favoriteSongs) { favoriteSong ->
                        FavoriteItem(favoriteSong) { _ ->
                            val song = Song(
                                id = favoriteSong.songId,
                                title = favoriteSong.title,
                                artist = favoriteSong.artist,
                                filePath = favoriteSong.filePath
                            )
                            musicControlListener?.onPlaySong(song)
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun FavoriteItem(favoriteSong: FavoriteSong, onClick: (FavoriteSong) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick(favoriteSong) }
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
                        text = favoriteSong.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = favoriteSong.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    
    override fun onDetach() {
        super.onDetach()
        musicControlListener = null
    }
}
