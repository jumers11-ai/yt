package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MusicProvider(val displayName: String, val brandColorHex: String) {
    SPOTIFY("Spotify", "#1DB954"),
    YOUTUBE("YouTube Music", "#FF0000"),
    SOUNDCLOUD("SoundCloud", "#FF5500"),
    DEEZER("Deezer", "#FF007F"),
    TIDAL("Tidal", "#00E6FF"),
    APPLE_MUSIC("Apple Music", "#FA243C")
}

data class ProviderAccount(
    val provider: MusicProvider,
    val isConnected: Boolean,
    val username: String? = null,
    val syncStatus: String = "Not Synced", // Synced, Syncing, Error, Not Synced
    val lastSyncTime: Long? = null
)

class OAuthService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kazik_oauth_prefs", Context.MODE_PRIVATE)
    
    private val _accountsState = MutableStateFlow<Map<MusicProvider, ProviderAccount>>(emptyMap())
    val accountsState: StateFlow<Map<MusicProvider, ProviderAccount>> = _accountsState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        val map = MusicProvider.values().associateWith { provider ->
            val isConnected = prefs.getBoolean("${provider.name}_connected", false)
            val username = prefs.getString("${provider.name}_username", null)
            val syncStatus = prefs.getString("${provider.name}_sync_status", "Not Connected") ?: "Not Connected"
            val lastSync = prefs.getLong("${provider.name}_last_sync", 0L)
            
            ProviderAccount(
                provider = provider,
                isConnected = isConnected,
                username = username,
                syncStatus = if (isConnected) syncStatus else "Not Connected",
                lastSyncTime = if (lastSync > 0L) lastSync else null
            )
        }
        _accountsState.value = map
    }

    fun connectAccount(provider: MusicProvider, username: String) {
        prefs.edit().apply {
            putBoolean("${provider.name}_connected", true)
            putString("${provider.name}_username", username)
            putString("${provider.name}_sync_status", "Synced")
            putLong("${provider.name}_last_sync", System.currentTimeMillis())
            apply()
        }
        loadAccounts()
    }

    fun disconnectAccount(provider: MusicProvider) {
        prefs.edit().apply {
            putBoolean("${provider.name}_connected", false)
            remove("${provider.name}_username")
            putString("${provider.name}_sync_status", "Not Connected")
            remove("${provider.name}_last_sync")
            apply()
        }
        loadAccounts()
    }

    fun triggerSync(provider: MusicProvider, onSyncComplete: () -> Unit = {}) {
        _accountsState.update { currentMap ->
            val account = currentMap[provider]
            if (account != null && account.isConnected) {
                val updatedMap = currentMap.toMutableMap()
                updatedMap[provider] = account.copy(syncStatus = "Syncing...")
                updatedMap
            } else {
                currentMap
            }
        }

        // Simulate syncing from APIs
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            prefs.edit().apply {
                putString("${provider.name}_sync_status", "Synced")
                putLong("${provider.name}_last_sync", System.currentTimeMillis())
                apply()
            }
            loadAccounts()
            onSyncComplete()
        }, 1500)
    }
}
