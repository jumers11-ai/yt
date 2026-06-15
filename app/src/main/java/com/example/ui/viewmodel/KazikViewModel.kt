package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KazikApplication
import com.example.data.model.CachedTrack
import com.example.data.model.Playlist
import com.example.data.service.MusicProvider
import com.example.data.service.ProviderAccount
import com.example.data.service.EqualizerState
import com.example.data.service.PlaybackState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class KazikViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as KazikApplication
    private val repository = app.repository
    val oauthService = app.oauthService
    val equalizerService = app.equalizerService
    val playbackService = app.playbackService

    // Navigation and Theme State
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Search, 2: Library, 3: Downloads, 4: Settings
    val currentTab = _currentTab.asStateFlow()

    private val _isLightTheme = MutableStateFlow(false)
    val isLightTheme = _isLightTheme.asStateFlow()

    // Playlist Details Overlay State (drill-down inside library)
    private val _activePlaylist = MutableStateFlow<Playlist?>(null)
    val activePlaylist = _activePlaylist.asStateFlow()

    val activePlaylistTracks = _activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else repository.getTracksForPlaylist(playlist.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks, Recent, Favorites, Downloads from Database
    val allTracks = repository.allTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favoriteTracks = repository.favoriteTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val downloadedTracks = repository.downloadedTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentTracks = repository.recentTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allPlaylists = repository.allPlaylists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Connected OAuth accounts
    val oauthAccounts = oauthService.accountsState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    // Equalizer
    val equalizerState = equalizerService.eqState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerState()
    )

    // Playback
    val playbackState = playbackService.state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState()
    )

    // Unified Search States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All") // All, Tracks, Albums, Playlists, Artists, Podcasts
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedProviderFilter = MutableStateFlow<MusicProvider?>(null) // null means all
    val selectedProviderFilter = _selectedProviderFilter.asStateFlow()

    val searchResults = combine(
        allTracks,
        _searchQuery,
        _selectedCategory,
        _selectedProviderFilter
    ) { tracks, query, category, provider ->
        if (query.isBlank() && category == "All" && provider == null) {
            return@combine tracks
        }
        
        tracks.filter { track ->
            val matchesQuery = query.isBlank() || 
                    track.title.contains(query, ignoreCase = true) || 
                    track.artist.contains(query, ignoreCase = true) || 
                    track.album.contains(query, ignoreCase = true)
            
            val matchesProvider = provider == null || track.provider.replace(" ", "").equals(provider.name.replace("_", ""), ignoreCase = true)
            
            // In seed data, simulate some metadata mappings
            val matchesCategory = when (category) {
                "All" -> true
                "Tracks" -> true
                "Albums" -> track.album.contains(query, ignoreCase = true) || !track.album.contains("Sessions")
                "Playlists" -> track.album.contains("Runway") || track.album.contains("Equinox")
                "Artists" -> track.artist.contains(query, ignoreCase = true)
                "Podcasts" -> track.durationMs > 300000L // Podcast simulation based on longer files
                else -> true
            }

            matchesQuery && matchesProvider && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI triggers and handlers
    fun selectTab(index: Int) {
        _currentTab.value = index
        _activePlaylist.value = null // reset playlist drill-down when tab changes
    }

    fun toggleTheme() {
        _isLightTheme.value = !_isLightTheme.value
    }

    fun playTrack(track: CachedTrack) {
        playbackService.playTrack(track, allTracks.value)
    }

    fun toggleFavorite(track: CachedTrack) {
        viewModelScope.launch {
            repository.setFavorite(track.id, !track.isFavorite)
        }
    }

    fun deleteTrackDownload(track: CachedTrack) {
        viewModelScope.launch {
            // Simulate deleting local files
            repository.deleteDownload(track.id)
        }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _activePlaylist.value = playlist
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            repository.addPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Int, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSearchCategory(category: String) {
        _selectedCategory.value = category
    }

    fun filterByProvider(provider: MusicProvider?) {
        _selectedProviderFilter.value = provider
    }

    // Storage calculation
    val storageUsageBytes = downloadedTracks.map { downloads ->
        downloads.sumOf { it.fileSize }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Trigger OAuth simulated login
    fun loginToProvider(provider: MusicProvider, username: String) {
        oauthService.connectAccount(provider, username)
    }

    fun logoutFromProvider(provider: MusicProvider) {
        oauthService.disconnectAccount(provider)
    }

    fun syncProvider(provider: MusicProvider) {
        oauthService.triggerSync(provider)
    }

    // Trigger downloading
    fun downloadTrack(track: CachedTrack) {
        viewModelScope.launch {
            // Check if provider is connected before allowing downloads (Optional/Encouraged)
            val isProdConnected = oauthAccounts.value.values.any { 
                it.provider.name.replace("_", "").equals(track.provider.replace(" ", ""), ignoreCase = true) && it.isConnected 
            }

            val mockSize = Random.nextLong(3_500_000, 8_000_000) // 3.5MB to 8MB
            val simulatedPath = "/data/user/0/${app.packageName}/files/downloads/${track.id}.mp3"
            repository.downloadTrack(track.id, simulatedPath, mockSize)
        }
    }
}
