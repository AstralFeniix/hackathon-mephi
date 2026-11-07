package ru.mephi.tikho.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.delay

/**
 * Небольшой контроллер прослушивания: держит MediaPlayer, сообщает позицию
 * и умеет перематывать по доле длительности или к конкретной реплике.
 */
@Stable
class AudioPreviewPlayer(
    private val context: Context,
    private val uriString: String?,
    initialDurationMs: Long,
) {
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false

    var isPlaying by mutableStateOf(false)
        private set

    var positionMs by mutableLongStateOf(0L)
        private set

    var durationMs by mutableLongStateOf(initialDurationMs.coerceAtLeast(0L))
        private set

    val isAvailable: Boolean
        get() = uriString != null

    fun toggle(): Boolean {
        if (!isAvailable) return false
        return try {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
                true
            } else {
                val player = ensurePlayer() ?: return false
                if (durationMs > 0 && positionMs >= durationMs - 60) {
                    player.seekTo(0)
                    positionMs = 0L
                }
                player.start()
                isPlaying = true
                true
            }
        } catch (_: Exception) {
            isPlaying = false
            release()
            false
        }
    }

    /** Перейти к нужной реплике и сразу начать воспроизведение. */
    fun playFrom(positionMs: Long): Boolean {
        if (!isAvailable) return false
        val target = positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))
        this.positionMs = target
        return try {
            val player = ensurePlayer() ?: return false
            player.seekTo(target.toInt())
            player.start()
            isPlaying = true
            true
        } catch (_: Exception) {
            isPlaying = false
            release()
            false
        }
    }

    fun seekTo(fraction: Float) {
        if (!isAvailable) return
        val duration = durationMs.takeIf { it > 0 } ?: return
        val target = (duration * fraction.coerceIn(0f, 1f)).toLong().coerceIn(0L, duration)
        positionMs = target
        if (prepared) {
            mediaPlayer?.seekTo(target.toInt())
        }
    }

    fun refreshPosition() {
        if (prepared) {
            val current = mediaPlayer?.currentPosition?.toLong() ?: return
            if (current >= 0L && current <= durationMs) {
                positionMs = current
            }
        }
    }

    fun release() {
        isPlaying = false
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        prepared = false
    }

    private fun ensurePlayer(): MediaPlayer? {
        if (prepared) return mediaPlayer
        val uri = uriString?.toUri() ?: return null
        val player = MediaPlayer()
        return try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            player.setDataSource(context, uri)
            player.setOnCompletionListener {
                isPlaying = false
                positionMs = durationMs
            }
            player.prepare()
            prepared = true
            mediaPlayer = player
            val actualDuration = player.duration.toLong()
            if (actualDuration > 0) durationMs = actualDuration
            if (positionMs > 0) player.seekTo(positionMs.toInt())
            player
        } catch (_: Exception) {
            runCatching { player.release() }
            null
        }
    }
}

@Composable
fun rememberAudioPreviewPlayer(
    uriString: String?,
    durationMillis: Long,
): AudioPreviewPlayer {
    val context = LocalContext.current
    val player = remember(uriString) {
        AudioPreviewPlayer(context.applicationContext, uriString, durationMillis)
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(player.isPlaying) {
        while (player.isPlaying) {
            player.refreshPosition()
            delay(80)
        }
    }

    return player
}
