package ru.mephi.tikho

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import ru.mephi.tikho.ui.Screen
import ru.mephi.tikho.ui.TikhoViewModel
import ru.mephi.tikho.ui.components.AppBackground
import ru.mephi.tikho.ui.screens.LibraryScreen
import ru.mephi.tikho.ui.screens.ProcessingScreen
import ru.mephi.tikho.ui.screens.ReadyScreen
import ru.mephi.tikho.ui.screens.RecordingScreen
import ru.mephi.tikho.ui.screens.ResultScreen
import ru.mephi.tikho.ui.theme.TikhoTheme

private const val PREFS_NAME = "tikho_prefs"
private const val KEY_DARK_THEME = "dark_theme"

class MainActivity : ComponentActivity() {

    private val viewModel: TikhoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            var darkTheme by rememberSaveable {
                mutableStateOf(preferences.getBoolean(KEY_DARK_THEME, false))
            }

            TikhoTheme(darkTheme = darkTheme) {
                SyncSystemBarIcons(isDarkTheme = darkTheme)
                TikhoApp(
                    viewModel = viewModel,
                    isDarkTheme = darkTheme,
                    onToggleTheme = {
                        darkTheme = !darkTheme
                        preferences.edit { putBoolean(KEY_DARK_THEME, darkTheme) }
                    },
                )
            }
        }
    }
}

@Composable
private fun SyncSystemBarIcons(isDarkTheme: Boolean) {
    val view = LocalView.current
    val window = (LocalContext.current as? Activity)?.window ?: return
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}

@Composable
private fun TikhoApp(
    viewModel: TikhoViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    BackHandler(enabled = viewModel.screen !is Screen.Library) {
        viewModel.back()
    }

    Box(Modifier.fillMaxSize()) {
        AppBackground()
        AnimatedContent(
            targetState = viewModel.screen,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "screen",
        ) { screen ->
            val screenModifier = Modifier.fillMaxSize()

            when (screen) {
                Screen.Library -> LibraryScreen(
                    recordings = viewModel.recordings,
                    onAudioPicked = viewModel::openReview,
                    onOpenRecording = viewModel::openRecording,
                    onStartRecording = viewModel::openRecorder,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    modifier = screenModifier,
                )

                Screen.Record -> RecordingScreen(
                    onBack = viewModel::back,
                    onRecorded = viewModel::openReview,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    modifier = screenModifier,
                )

                is Screen.Review -> ReadyScreen(
                    audio = screen.audio,
                    onBack = viewModel::back,
                    onTranscribe = { viewModel.startTranscription(screen.audio) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    modifier = screenModifier,
                )

                is Screen.Processing -> ProcessingScreen(
                    audio = screen.audio,
                    progress = viewModel.progress,
                    visibleUtterances = viewModel.visibleUtterances,
                    error = viewModel.processingError,
                    onRetry = viewModel::retryTranscription,
                    onBack = viewModel::back,
                    onCancel = viewModel::cancelTranscription,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    modifier = screenModifier,
                )

                is Screen.Result -> ResultScreen(
                    recording = screen.recording,
                    onBack = viewModel::back,
                    onDelete = { viewModel.deleteRecording(screen.recording) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    modifier = screenModifier,
                )
            }
        }
    }
}
