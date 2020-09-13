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

    fun Date.add(field: Int, amount: Int) = Calendar.getInstance().apply {
        time = this@add
        add(field, amount)
    }.time
}