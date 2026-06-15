package com.example

import android.app.Application
import com.example.data.database.KazikDatabase
import com.example.data.model.SeedData
import com.example.data.repository.KazikRepository
import com.example.data.service.EqualizerService
import com.example.data.service.MediaPlaybackService
import com.example.data.service.OAuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KazikApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { KazikDatabase.getDatabase(this) }
    val repository by lazy { KazikRepository(database.trackDao(), database.playlistDao()) }
    val oauthService by lazy { OAuthService(this) }
    val equalizerService by lazy { EqualizerService(this) }
    val playbackService by lazy { MediaPlaybackService(repository) }

    override fun onCreate() {
        super.onCreate()
        
        // Seed initial tracks if database is empty
        applicationScope.launch {
            val trackDao = database.trackDao()
            val existing = trackDao.getTrackById("spotify_1")
            if (existing == null) {
                trackDao.insertTracks(SeedData.initialTracks)
                
                // Create a couple of default seed playlists
                val playlistDao = database.playlistDao()
                val favId = playlistDao.insertPlaylist(
                    com.example.data.model.Playlist(
                        name = "Late Night Vibe",
                        description = "Smooth cyber and retro-synth wave sounds."
                    )
                )
                playlistDao.addTrackToPlaylist(
                    com.example.data.model.PlaylistTrackCrossRef(
                        playlistId = favId.toInt(),
                        trackId = "spotify_1"
                    )
                )
                playlistDao.addTrackToPlaylist(
                    com.example.data.model.PlaylistTrackCrossRef(
                        playlistId = favId.toInt(),
                        trackId = "youtube_1"
                    )
                )
            }
        }
    }
}
