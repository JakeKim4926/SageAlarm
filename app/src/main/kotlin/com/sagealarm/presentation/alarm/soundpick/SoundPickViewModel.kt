package com.sagealarm.presentation.alarm.soundpick

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundPickViewModel @Inject constructor() : ViewModel() {

    private var player: ExoPlayer? = null
    private var positionJob: Job? = null

    private val _previewUri = MutableStateFlow<String?>(null)
    val previewUri: StateFlow<String?> = _previewUri.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    fun togglePreview(context: Context, uri: String) {
        if (_previewUri.value == uri) {
            player?.let { p ->
                if (p.isPlaying) {
                    p.pause()
                    _isPlaying.value = false
                    positionJob?.cancel()
                } else {
                    p.play()
                    _isPlaying.value = true
                    startPositionUpdates()
                }
            }
        } else {
            startNewPreview(context, uri)
        }
    }

    fun seekTo(fraction: Float) {
        player?.let { p ->
            val duration = p.duration
            if (duration > 0L) {
                p.seekTo((fraction * duration).toLong())
                _progress.value = fraction
            }
        }
    }

    fun stopPreview() {
        positionJob?.cancel()
        player?.release()
        player = null
        _previewUri.value = null
        _isPlaying.value = false
        _progress.value = 0f
    }

    private fun startNewPreview(context: Context, uri: String) {
        positionJob?.cancel()
        player?.release()

        val newPlayer = ExoPlayer.Builder(context.applicationContext).build()
        player = newPlayer
        newPlayer.setMediaItem(MediaItem.fromUri(uri))
        newPlayer.prepare()
        newPlayer.play()
        _previewUri.value = uri
        _isPlaying.value = true
        _progress.value = 0f
        startPositionUpdates()
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (true) {
                delay(POSITION_UPDATE_INTERVAL_MS)
                val p = player ?: break
                when {
                    p.isPlaying -> {
                        val duration = p.duration
                        if (duration > 0L) {
                            _progress.value = p.currentPosition.toFloat() / duration
                        }
                    }
                    p.playWhenReady && p.playbackState == Player.STATE_BUFFERING -> {
                        // 버퍼링 중: isPlaying은 false지만 곧 재생될 예정이므로 대기
                    }
                    else -> {
                        _isPlaying.value = false
                        if (p.playbackState == Player.STATE_ENDED) {
                            _progress.value = 0f
                        }
                        break
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MS = 100L
    }
}
