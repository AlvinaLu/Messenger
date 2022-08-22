package com.android.example.messenger.utils.message

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

fun String.toTime(): String {
    val dateNow = LocalDateTime.now()
    val date = LocalDateTime.parse(this)
        .atZone(ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneId.systemDefault())
        .toLocalDateTime();

    if ((dateNow.minusMinutes(5L)) < date) {
        return " recently"
    } else if (date.toLocalDate() != dateNow.toLocalDate() && Duration.between(date, dateNow).toDays() < 7) {
        return " on " + date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)
    } else if (Duration.between(date, dateNow).toDays() >= 7) {
        return " " + DateTimeFormatter.ofPattern("dd.MMM.yyyy").format(date)
    } else {
        return " at " + DateTimeFormatter.ofPattern("HH:mm").format(date)
    }

}

fun String.toTimeMessage(): String {
    val dateNow = LocalDateTime.now()
    val date = LocalDateTime.parse(this)
        .atZone(ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneId.systemDefault())
        .toLocalDateTime();

    if (date.toLocalDate() != dateNow.toLocalDate() && Duration.between(date, dateNow).toDays() < 7) {
        return " " + date.dayOfWeek.getDisplayName(
            TextStyle.SHORT,
            Locale.UK
        ) + " " + DateTimeFormatter.ofPattern("HH:mm").format(date)
    } else if (Duration.between(date, dateNow).toDays() >= 7) {
        return " " + DateTimeFormatter.ofPattern("HH:mm")
            .format(date) + " " + DateTimeFormatter.ofPattern("dd.MMM.yyyy").format(date)
    } else {
        return " " + DateTimeFormatter.ofPattern("HH:mm").format(date)
    }
}

fun String.cut(max: Int): String {
    if (this.length > max) {
        var name = this.substring(0, max)
        name += "..."
        return name
    } else {
        return this
    }
}
