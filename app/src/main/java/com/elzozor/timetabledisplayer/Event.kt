package com.elzozor.timetabledisplayer

import com.elzozor.yoda.events.EventWrapper
import java.util.*

open class Event (
    val begin : Date,
    val end : Date,
    val title : String,
    val description : String) : EventWrapper()
{
    override fun begin() = begin

    override fun end() = end
}