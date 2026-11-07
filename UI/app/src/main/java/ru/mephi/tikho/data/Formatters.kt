package ru.mephi.tikho.data

import java.util.Locale
import kotlin.math.roundToInt

/** "06:24" — минуты и секунды для длительности и прогресса. */
fun formatClock(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

/** "8,2 МБ" — размер выбранного файла. */
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val kilobytes = bytes / 1024.0
    if (kilobytes < 1024.0) {
        return "${kilobytes.roundToInt()} КБ"
    }
    val megabytes = kilobytes / 1024.0
    return String.format(Locale.US, "%.1f", megabytes).replace('.', ',') + " МБ"
}

/** "6 мин" — компактная длительность для списка записей. */
fun formatMinutesShort(millis: Long): String {
    val minutes = (millis / 60_000.0).roundToInt().coerceAtLeast(1)
    return "$minutes мин"
}

fun formatProcessed(progress: Float, totalMillis: Long): String {
    val clamped = progress.coerceIn(0f, 1f)
    val seconds = (totalMillis * clamped / 1000f).roundToInt()
    return formatClock(seconds * 1000L)
}

fun remainingLabel(progress: Float, totalMillis: Long): String {
    val remainingSeconds = ((1f - progress.coerceIn(0f, 1f)) * totalMillis / 1000L).toLong()
    return when {
        remainingSeconds <= 45L -> "Осталось меньше минуты"
        remainingSeconds <= 160L -> "Осталось около минуты"
        else -> "Осталось около двух минут"
    }
}
