package com.elzozor.yoda.utils

import android.content.Context
import android.util.TypedValue

object NumberExtensions {

    fun Number.toDp(context: Context) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )

}