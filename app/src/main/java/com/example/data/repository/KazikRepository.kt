package com.example.data.repository

import com.example.data.database.PlaylistDao
import com.example.data.database.TrackDao
import com.example.data.model.CachedTrack
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef
import kotlinx.coroutines.flow.Flow

class KazikRepository(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao
) {
    val allTracks: Flow<List<CachedTrack>> = trackDao.getAllTracksFlow()
    val favoriteTracks: Flow<List<CachedTrack>> = trackDao.getFavoriteTracksFlow()
    val downloadedTracks: Flow<List<CachedTrack>> = trackDao.getDownloadedTracksFlow()
    val recentTracks: Flow<List<CachedTrack>> = trackDao.getRecentTracksFlow()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylistsFlow()

    suspend fun getTrackById(id: String): CachedTrack? = trackDao.getTrackById(id)

    suspend fun insertTrack(track: CachedTrack) = trackDao.insertTrack(track)

    suspend fun insertTracks(tracks: List<CachedTrack>) = trackDao.insertTracks(tracks)

    suspend fun setFavorite(id: String, isFavorite: Boolean) {
        trackDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun downloadTrack(id: String, filePath: String, sizeL: Long) {
        trackDao.updateDownloadedStatus(id, isDownloaded = true, localFilePath = filePath, fileSize = sizeL)
    }

    suspend fun deleteDownload(id: String) {
        trackDao.updateDownloadedStatus(id, isDownloaded = false, localFilePath = null, fileSize = 0L)
    }

    suspend fun incrementPlayCount(id: String) {
        trackDao.incrementPlayCountAndSetTimestamp(id, System.currentTimeMillis())
    }

    suspend fun addPlaylist(name: String, description: String? = null): Long {
        val playlist = Playlist(name = name, description = description)
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlistId: Int) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: String) {
        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Int): Flow<List<CachedTrack>> {
        return playlistDao.getTracksForPlaylistFlow(playlistId)
    }
}
