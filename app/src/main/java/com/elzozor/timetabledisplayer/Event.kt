package com.elzozor.timetabledisplayer

import com.elzozor.yoda.events.EventWrapper
import java.util.*

open class Event (
    val begin : Date,
    val end : Date,
    val title : String,
    val description : String,
    val places : Array<Date>,
    val id: Int)
    : Comparable<Event>, EventWrapper()
{
    constructor(event: Event) : this(
        Date(event.begin.time),
        Date(event.end.time),
        String(event.title.toCharArray()),
        String(event.description.toCharArray()),
        event.places,
        event.id
    )

    override fun compareTo(other: Event) =
        id.compareTo(other.id)

    override fun begin() = begin

    override fun end() = end
}