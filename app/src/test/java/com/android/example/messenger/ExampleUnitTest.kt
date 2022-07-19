package com.android.example.messenger

import org.junit.Assert.*
import org.junit.Test
import java.text.DateFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoField

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    @Test

    fun checkGetTime(){
        println(getTime("2022-07-03T11:29:10.548933"))

    }
    fun getTime(createdAt: String): String {
        val dateNow = LocalDateTime.now()
        val date = LocalDateTime.parse(createdAt)
        if ((dateNow.minusDays(1)) > date) {
            val day = LocalDateTime.parse(createdAt)
            val dayofweek: DayOfWeek = day.getDayOfWeek()
            val symbols = DateFormatSymbols();
            val dayNames: Array<String> = symbols.getShortWeekdays()
            return "$dayNames"
        }
        val hours = LocalDateTime.parse(createdAt).get(ChronoField.HOUR_OF_DAY)
        val minutes = LocalDateTime.parse(createdAt).get(ChronoField.MINUTE_OF_HOUR)
        return "$hours:$minutes"
    }
}