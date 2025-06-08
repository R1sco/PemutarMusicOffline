# Pemutar Musik Offline

Aplikasi pemutar musik offline untuk Android yang dibangun dengan Kotlin dan Jetpack Compose. Aplikasi ini memungkinkan pengguna untuk memutar lagu yang tersimpan di perangkat mereka dengan antarmuka yang modern dan mudah digunakan.

## Fitur

- Memutar musik offline dari penyimpanan perangkat
- Dukungan untuk kontrol pemutaran (play, pause, stop)
- Dukungan untuk headset controls
- Notifikasi media dengan kontrol pemutaran
- Daftar putar lagu
- Favorit lagu
- Pencarian lagu
- Tema gelap/terang (jika diimplementasikan)

# TODO

- Fungsi Background
- Notif

## Komponen Android yang Digunakan

### 1. Service
- `MusicService`: Menangani pemutaran musik di latar belakang

### 2. Broadcast Receiver
- `HeadsetReceiver`: Mendeteksi perubahan headset

### 3. Notification
- `MediaNotificationManager`: Menampilkan notifikasi media

### 4. Room Database
- Menyimpan daftar lagu favorit
- Menggunakan DAO untuk operasi database

### 5. Permission
- Izin akses penyimpanan dan pemutaran latar belakang

### 6. MediaSession
- Mengintegrasikan dengan sistem media Android
- Mendukung kontrol eksternal

### 7. Komponen UI
- Jetpack Compose untuk antarmuka modern
- Fragment untuk modularitas
- ViewModel untuk manajemen data UI

## Teknologi yang Digunakan

- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - Untuk UI modern
- **AndroidX** - Komponen Android terbaru
- **Room** - Untuk penyimpanan data lokal (daftar favorit)
- **Media3** - Untuk pemutaran audio
- **Coroutines & Flow** - Untuk operasi asinkron
- **Dependency Injection** - Hilt (jika diimplementasikan)

## Persyaratan Sistem

- Android 6.0 (API level 24) atau lebih tinggi
- Izin baca penyimpanan eksternal

## Instalasi

1. Clone repositori ini
2. Buka proyek di Android Studio
3. Tunggu proses sinkronisasi Gradle selesai
4. Tekan tombol "Run" untuk menjalankan di perangkat atau emulator

## Struktur Proyek

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/pemutarmusicoffline/
│   │   │   ├── MainActivity.kt         # Aktivitas utama
│   │   │   ├── MusicService.kt         # Layanan untuk pemutaran musik
│   │   │   ├── Song.kt                 # Model data lagu
│   │   │   ├── SongUtils.kt            # Utilitas untuk mengambil lagu
│   │   │   ├── database/               # Komponen database Room
│   │   │   ├── fragment/               # Fragment untuk UI
│   │   │   ├── notification/           # Manajer notifikasi
│   │   │   ├── receiver/               # BroadcastReceiver
│   │   │   └── ui/theme/               # Tema dan gaya
│   │   └── res/                        # Resource aplikasi
│   └── test/                           # Unit test
└── build.gradle.kts                    # Konfigurasi build
```

## Cara Penggunaan

1. Berikan izin akses penyimpanan saat diminta
2. Pilih lagu dari daftar
3. Gunakan kontrol pemutaran di layar atau dari notifikasi
4. Tambahkan lagu ke favorit dengan menekan ikon hati
5. Cari lagu menggunakan fitur pencarian

## Kontribusi

Kontribusi terbuka untuk pengembangan lebih lanjut. Silakan buat issue atau pull request.

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).
