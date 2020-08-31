package com.elzozor.yoda.utils

import java.util.*

object DateExtensions {
    fun Date?.get(field: Int): Int? {
        return this?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.get(field)
        }
    }
}