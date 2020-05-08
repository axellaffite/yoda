package com.elzozor.yoda.utils

import java.util.*
import java.util.Calendar.*
import kotlin.math.abs

class TimeUtils {
    companion object {
        private fun getCalendar() = Calendar.getInstance()

        fun onlyTimeAsMs(date: Date): Long {
            val t = getCalendar().apply {
                time = date
            }
            val hours = t.get(HOUR_OF_DAY)
            val minutes = t.get(MINUTE)
            val seconds = t.get(SECOND)

            return (((hours * 60L + minutes) * 60L + seconds) * 1000L)
        }


        fun onlytime(timeInMillis: Long) =
            getCalendar().apply {
                this.timeInMillis = timeInMillis
                set(0, 0, 0)
            }.time.time

        fun rangeBetweenDates(dateA: Date, dateB: Date) : Long =
            abs(dateA.time - dateB.time)

        fun millisecondsToHours(value: Long) = value / 3.6e6f
    }
}