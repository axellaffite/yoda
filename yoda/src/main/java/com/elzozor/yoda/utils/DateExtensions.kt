package com.elzozor.yoda.utils

import java.util.*

object DateExtensions {
    fun Date.get(field: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.time = this
        return calendar.get(field)
    }

    fun Date.resetTime() : Date {
        return Calendar.getInstance().apply {
            time = this@resetTime

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun Date.add(field: Int, amount: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.apply {
            time = this@add
            add(field, amount)
        }

        return calendar.time
    }

    fun Date.setup(
        year: Int,
        month: Int = Calendar.JANUARY,
        day: Int = 1,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        millisecond: Int = 0
    ): Date {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, millisecond)
        }

        return calendar.time
    }
}