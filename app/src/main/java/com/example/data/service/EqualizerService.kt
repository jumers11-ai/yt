package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EqualizerState(
    val isEnabled: Boolean = true,
    val selectedPreset: String = "Normal",
    val bands: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), // values from -12 to +12 dB
    val bassBoost: Int = 30, // 0 to 100
    val virtualizer: Int = 20, // 0 to 100
    val playbackSpeed: Float = 1.0f,
    val sleepTimerRemainingSeconds: Int? = null // null means inactive
)

class EqualizerService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kazik_eq_prefs", Context.MODE_PRIVATE)

    private val _eqState = MutableStateFlow(EqualizerState())
    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()

    private val presetMap = mapOf(
        "Normal" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Rock" to listOf(4, 3, -1, -2, -1, 1, 3, 4, 3, 2),
        "Pop" to listOf(-1, 1, 2, 3, 2, -1, -2, -1, 1, 2),
        "Jazz" to listOf(2, 1, 0, 1, -1, -1, 0, 1, 2, 3),
        "Classical" to listOf(3, 2, 1, 1, -1, -1, -1, 1, 2, 3),
        "Electronic" to listOf(4, 2, 0, -1, -2, 1, 1, 2, 3, 4)
    )

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val isEnabled = prefs.getBoolean("eq_enabled", true)
        val selectedPreset = prefs.getString("selected_preset", "Normal") ?: "Normal"
        val bassBoost = prefs.getInt("bass_boost", 30)
        val virtualizer = prefs.getInt("virtualizer", 20)
        val speed = prefs.getFloat("playback_speed", 1.0f)

        val activeBands = mutableListOf<Int>()
        val defaultBands = presetMap[selectedPreset] ?: presetMap["Normal"]!!
        for (i in 0..9) {
            val bandVal = prefs.getInt("band_$i", defaultBands.getOrElse(i) { 0 })
            activeBands.add(bandVal)
        }

        _eqState.value = EqualizerState(
            isEnabled = isEnabled,
            selectedPreset = selectedPreset,
            bands = activeBands,
            bassBoost = bassBoost,
            virtualizer = virtualizer,
            playbackSpeed = speed
        )
    }

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
        _eqState.update { it.copy(isEnabled = enabled) }
    }

    fun applyPreset(presetName: String) {
        if (presetName == "Custom") {
            _eqState.update { it.copy(selectedPreset = "Custom") }
            prefs.edit().putString("selected_preset", "Custom").apply()
            return
        }

        val bands = presetMap[presetName] ?: presetMap["Normal"]!!
        val editor = prefs.edit().putString("selected_preset", presetName)
        bands.forEachIndexed { index, value ->
            editor.putInt("band_$index", value)
        }
        editor.apply()

        _eqState.update {
            it.copy(
                selectedPreset = presetName,
                bands = bands
            )
        }
    }

    fun updateBand(index: Int, valDb: Int) {
        prefs.edit().putInt("band_$index", valDb).putString("selected_preset", "Custom").apply()
        _eqState.update { currentState ->
            val newBands = currentState.bands.toMutableList()
            if (index in newBands.indices) {
                newBands[index] = valDb
            }
            currentState.copy(
                selectedPreset = "Custom",
                bands = newBands
            )
        }
    }

    fun setBassBoost(value: Int) {
        prefs.edit().putInt("bass_boost", value).apply()
        _eqState.update { it.copy(bassBoost = value) }
    }

    fun setVirtualizer(value: Int) {
        prefs.edit().putInt("virtualizer", value).apply()
        _eqState.update { it.copy(virtualizer = value) }
    }

    fun setPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat("playback_speed", speed).apply()
        _eqState.update { it.copy(playbackSpeed = speed) }
    }

    fun setSleepTimer(minutes: Int?) {
        if (minutes == null) {
            _eqState.update { it.copy(sleepTimerRemainingSeconds = null) }
        } else {
            _eqState.update { it.copy(sleepTimerRemainingSeconds = minutes * 60) }
            startTimerCountdown()
        }
    }

    private var countdownThread: Thread? = null

    private fun startTimerCountdown() {
        countdownThread?.interrupt()
        countdownThread = Thread {
            try {
                while (true) {
                    Thread.sleep(1000)
                    var shouldStop = false
                    _eqState.update { current ->
                        val remaining = current.sleepTimerRemainingSeconds
                        if (remaining == null || remaining <= 0) {
                            shouldStop = true
                            current.copy(sleepTimerRemainingSeconds = null)
                        } else {
                            current.copy(sleepTimerRemainingSeconds = remaining - 1)
                        }
                    }
                    if (shouldStop) break
                }
            } catch (e: InterruptedException) {
                // Interrupted
            }
        }
        countdownThread?.start()
    }
}
