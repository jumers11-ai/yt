package com.example.data.database

import androidx.room.*
import com.example.data.model.CachedTrack
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM cached_tracks ORDER BY title ASC")
    fun getAllTracksFlow(): Flow<List<CachedTrack>>

    @Query("SELECT * FROM cached_tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracksFlow(): Flow<List<CachedTrack>>

    @Query("SELECT * FROM cached_tracks WHERE isDownloaded = 1 ORDER BY title ASC")
    fun getDownloadedTracksFlow(): Flow<List<CachedTrack>>

    @Query("SELECT * FROM cached_tracks WHERE playCount > 0 ORDER BY lastPlayedTimestamp DESC")
    fun getRecentTracksFlow(): Flow<List<CachedTrack>>

    @Query("SELECT * FROM cached_tracks WHERE id = :id")
    suspend fun getTrackById(id: String): CachedTrack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: CachedTrack)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(tracks: List<CachedTrack>)

    @Update
    suspend fun updateTrack(track: CachedTrack)

    @Query("DELETE FROM cached_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("UPDATE cached_tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE cached_tracks SET isDownloaded = :isDownloaded, localFilePath = :localFilePath, fileSize = :fileSize WHERE id = :id")
    suspend fun updateDownloadedStatus(id: String, isDownloaded: Boolean, localFilePath: String?, fileSize: Long)

    @Query("UPDATE cached_tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementPlayCountAndSetTimestamp(id: String, timestamp: Long)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsFlow(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: String)

    @Query("""
        SELECT * FROM cached_tracks 
        INNER JOIN playlist_track_cross_ref ON cached_tracks.id = playlist_track_cross_ref.trackId 
        WHERE playlist_track_cross_ref.playlistId = :playlistId
    """)
    fun getTracksForPlaylistFlow(playlistId: Int): Flow<List<CachedTrack>>
}
