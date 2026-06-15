package com.example.data.service

import com.example.data.model.CachedTrack
import com.example.data.repository.KazikRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class PlaybackState(
    val currentTrack: CachedTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val queue: List<CachedTrack> = emptyList(),
    val currentIndex: Int = -1,
    val visualizerWaves: List<Float> = List(16) { 0.1f }
)

enum class RepeatMode {
    NONE, ONE, ALL
}

class MediaPlaybackService(
    private val repository: KazikRepository
) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Start monitoring playback ticks and visualizer fluctuations
        startPlaybackLoop()
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive) {
                delay(200)
                if (_state.value.isPlaying && _state.value.currentTrack != null) {
                    val track = _state.value.currentTrack!!
                    val nextPos = _state.value.currentPositionMs + 200L
                    
                    if (nextPos >= track.durationMs) {
                        // Song ended - handle repeat / skip
                        handleSongCompletion()
                    } else {
                        // Regular tick
                        _state.update { current ->
                            current.copy(
                                currentPositionMs = nextPos,
                                visualizerWaves = List(16) { Random.nextFloat() * 0.8f + 0.2f }
                            )
                        }
                    }
                } else {
                    // Turn down visualizer when paused
                    if (_state.value.visualizerWaves.any { it > 0.1f }) {
                        _state.update { current ->
                            current.copy(
                                visualizerWaves = current.visualizerWaves.map { it * 0.7f }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleSongCompletion() {
        when (_state.value.repeatMode) {
            RepeatMode.ONE -> {
                _state.update { it.copy(currentPositionMs = 0L) }
            }
            RepeatMode.ALL -> {
                skipNext()
            }
            RepeatMode.NONE -> {
                if (_state.value.currentIndex < _state.value.queue.lastIndex) {
                    skipNext()
                } else {
                    _state.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                }
            }
        }
    }

    fun playTrack(track: CachedTrack, customQueue: List<CachedTrack> = emptyList()) {
        val finalQueue = if (customQueue.isNotEmpty()) customQueue else listOf(track)
        val idx = finalQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        scope.launch {
            repository.incrementPlayCount(track.id)
        }

        _state.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                queue = finalQueue,
                currentIndex = idx
            )
        }
    }

    fun playPause() {
        _state.update {
            if (it.currentTrack == null && it.queue.isNotEmpty()) {
                val first = it.queue[0]
                scope.launch { repository.incrementPlayCount(first.id) }
                it.copy(
                    currentTrack = first,
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = first.durationMs,
                    currentIndex = 0
                )
            } else {
                it.copy(isPlaying = !it.isPlaying)
            }
        }
    }

    fun skipNext() {
        val s = _state.value
        if (s.queue.isEmpty()) return
        
        var nextIdx = s.currentIndex + 1
        if (s.isShuffleEnabled) {
            nextIdx = Random.nextInt(s.queue.size)
        } else if (nextIdx >= s.queue.size) {
            nextIdx = 0
        }

        val nextTrack = s.queue[nextIdx]
        scope.launch { repository.incrementPlayCount(nextTrack.id) }

        _state.update {
            it.copy(
                currentTrack = nextTrack,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = nextTrack.durationMs,
                currentIndex = nextIdx
            )
        }
    }

    fun skipPrevious() {
        val s = _state.value
        if (s.queue.isEmpty()) return

        var prevIdx = s.currentIndex - 1
        if (prevIdx < 0) {
            prevIdx = s.queue.size - 1
        }

        val prevTrack = s.queue[prevIdx]
        scope.launch { repository.incrementPlayCount(prevTrack.id) }

        _state.update {
            it.copy(
                currentTrack = prevTrack,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = prevTrack.durationMs,
                currentIndex = prevIdx
            )
        }
    }

    fun toggleShuffle() {
        _state.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun cycleRepeatMode() {
        _state.update {
            val nextMode = when (it.repeatMode) {
                RepeatMode.NONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.NONE
            }
            it.copy(repeatMode = nextMode)
        }
    }

    fun seekTo(positionMs: Long) {
        _state.update {
            val validPos = positionMs.coerceIn(0L, it.durationMs)
            it.copy(currentPositionMs = validPos)
        }
    }

    fun addToQueue(track: CachedTrack) {
        _state.update {
            if (it.queue.any { q -> q.id == track.id }) {
                it
            } else {
                val newQueue = it.queue.toMutableList().apply { add(track) }
                it.copy(
                    queue = newQueue,
                    currentIndex = if (it.currentIndex == -1) 0 else it.currentIndex,
                    currentTrack = if (it.currentTrack == null) track else it.currentTrack
                )
            }
        }
    }

    fun clearQueue() {
        _state.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                currentPositionMs = 0L,
                durationMs = 0L,
                queue = emptyList(),
                currentIndex = -1
            )
        }
    }
}
