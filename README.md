# Pemutar Musik Offline

An offline music player application for Android built with Kotlin and Jetpack Compose. This application allows users to play music stored on their devices with a modern and user-friendly interface.

## Features

- Play offline music from device storage
- Playback controls support (play, pause, stop)
- Headset controls support
- Media notification with playback controls
- Playlist
- Favorite songs
- Song search

# TODO

- Background playback function
- Notification

## Android Components Used

### 1. Service
- `MusicService`: Handles background music playback

### 2. Broadcast Receiver
- `HeadsetReceiver`: Detects headset changes

### 3. Notification
- `MediaNotificationManager`: Displays media notifications

### 4. Room Database
- Stores favorite songs list
- Uses DAO for database operations

### 5. Permissions
- Storage access and background playback permissions

### 6. MediaSession
- Integrates with Android media system
- Supports external controls

### 7. UI Components
- Jetpack Compose for modern interface
- Fragments for modularity
- ViewModel for UI data management

## Technologies Used

- **Kotlin** - Primary programming language
- **Jetpack Compose** - For modern UI
- **AndroidX** - Latest Android components
- **Room** - For local data storage (favorites list)
- **Media3** - For audio playback
- **Coroutines & Flow** - For asynchronous operations
- **Dependency Injection** - Hilt (if implemented)

## System Requirements

- Android 6.0 (API level 24) or higher
- External storage read permission

## Installation

1. Clone this repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Press the "Run" button to run on a device or emulator

## Teknologi yang Digunakan

- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - Untuk UI modern
- **AndroidX** - Komponen Android terbaru
- **Room** - Untuk penyimpanan data lokal (daftar favorit)
- **Media3** - Untuk pemutaran audio
- **Coroutines & Flow** - Untuk operasi asinkron
- **Dependency Injection** - Hilt (jika diimplementasikan)

## System Requirements

- Android 6.0 (API level 24) or higher
- External storage read permission


## Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/pemutarmusicoffline/
│   │   │   ├── MainActivity.kt         # Main activity
│   │   │   ├── MusicService.kt         # Service for music playback
│   │   │   ├── Song.kt                 # Song data model
│   │   │   ├── SongUtils.kt            # Utilities retriving songs
│   │   │   ├── database/               # Component database Room
│   │   │   ├── fragment/               # Fragment For UI
│   │   │   ├── notification/           # Manager Notification
│   │   │   ├── receiver/               # BroadcastReceiver
│   │   │   └── ui/theme/               # Tema dan gaya
│   │   └── res/                        # Resource Apps
│   └── test/                           # Unit test
└── build.gradle.kts                    # Configuration build
```


## Lisensi

This project is licensed under the [MIT License](LICENSE).
