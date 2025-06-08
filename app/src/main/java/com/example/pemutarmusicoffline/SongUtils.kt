package com.example.pemutarmusicoffline

import android.content.Context
import android.content.Intent
import android.provider.MediaStore

// Fungsi untuk memuat daftar lagu dari MediaStore
fun loadSongs(context: Context): List<Song> {
    val songs = mutableListOf<Song>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA
    )
    val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
    val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

    context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn)
            val artist = cursor.getString(artistColumn)
            val filePath = cursor.getString(dataColumn)
            songs.add(Song(id, title, artist, filePath))
        }
    }
    return songs
}

// Fungsi untuk memulai pemutaran lagu
fun playSong(context: Context, song: Song) {
    val intent = Intent(context, MusicService::class.java).apply {
        action = MusicService.ACTION_PLAY
        putExtra(MusicService.EXTRA_SONG_URI, song.filePath)
        putExtra(MusicService.EXTRA_SONG_TITLE, song.title)
        putExtra(MusicService.EXTRA_SONG_ARTIST, song.artist)
        putExtra(MusicService.EXTRA_SONG_ID, song.id)
    }
    context.startService(intent)
}

// Fungsi untuk menjeda pemutaran lagu
fun pauseSong(context: Context) {
    val intent = Intent(context, MusicService::class.java).apply {
        action = MusicService.ACTION_PAUSE
    }
    context.startService(intent)
}

// Fungsi untuk menghentikan pemutaran lagu
fun stopSong(context: Context) {
    val intent = Intent(context, MusicService::class.java).apply {
        action = MusicService.ACTION_STOP
    }
    context.startService(intent)
}
