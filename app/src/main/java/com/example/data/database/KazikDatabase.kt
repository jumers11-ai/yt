package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CachedTrack
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef

@Database(
    entities = [CachedTrack::class, Playlist::class, PlaylistTrackCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class KazikDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: KazikDatabase? = null

        fun getDatabase(context: Context): KazikDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KazikDatabase::class.java,
                    "kazik_music_player_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
