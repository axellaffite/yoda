package com.elzozor.yoda.events

import java.util.*

abstract class EventWrapper {
    var x = 0.0f
    var width = 0

    /**
     * @return the start date of the event
     */
    abstract fun begin() : Date

    /**
     * @return the end date of the event
     */
    abstract fun end() : Date

    /**
     * @return Whether the event is all the day or not
     */
    abstract fun isAllDay() : Boolean
}