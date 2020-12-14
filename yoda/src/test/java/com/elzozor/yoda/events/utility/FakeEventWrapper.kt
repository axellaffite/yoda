package com.elzozor.yoda.events.utility

import com.elzozor.yoda.events.EventWrapper
import java.util.*

/**
 * A fake Event to test our
 * implementation
 *
 * @property _begin The beginning of the event
 * @property _end The end of the event
 * @property _allDay Weather the event is all day or not
 */
class FakeEventWrapper(
    private val _begin: Date,
    private val _end: Date,
    private val _allDay: Boolean
) : EventWrapper() {
    override fun begin() = _begin
    override fun end() = _end
    override fun isAllDay() = _allDay
}