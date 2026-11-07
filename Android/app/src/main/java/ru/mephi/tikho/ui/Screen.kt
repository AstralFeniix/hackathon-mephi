package ru.mephi.tikho.ui

import ru.mephi.tikho.model.AudioItem
import ru.mephi.tikho.model.Recording

/** Навигация приложения без внешней библиотеки. */
sealed interface Screen {
    data object Library : Screen

    data object Record : Screen

    data class Review(val audio: AudioItem) : Screen

    data class Processing(val audio: AudioItem) : Screen

    data class Result(val recording: Recording) : Screen
}
