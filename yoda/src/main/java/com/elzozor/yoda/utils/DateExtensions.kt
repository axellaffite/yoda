package com.elzozor.yoda.utils

import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object DateExtensions {
    fun Date?.get(field: Int): Int? {
        return this?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.get(field)
        }
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

    @ExperimentalTime
    operator fun Date.plus(duration: Duration) = Date(
        time + duration.inMilliseconds.toLong()
    )
}