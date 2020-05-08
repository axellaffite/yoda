package com.elzozor.yoda.events

import java.util.*
import kotlin.collections.ArrayList

class EventOrganizer (events: List<EventWrapper>) {

    private val events : List<CollisionEventWrapper>

    init {
        this.events = events.fold(listOf<CollisionEventWrapper>()) { acc, event ->
            acc + CollisionEventWrapper(event)
        }.sortedWith(Comparator { o1, o2 ->
            var comp = o1.begin().compareTo(o2.begin())
            if (comp == 0) {
                comp = o2.end().compareTo(o1.end())
            }

            comp
        })
    }

    fun orgnanize(width: Int) : List<EventWrapper> {
        val columns = ArrayList<ArrayList<CollisionEventWrapper>>()
        var lastEventEnding : Date? = null

        events.forEach { event ->
            if (lastEventEnding != null && event.begin() >= lastEventEnding) {
                packEvents(columns, width)
                columns.clear()
                lastEventEnding = null
            }

            var placed = false
            for (i in 0 until columns.size) {
                val column = columns[i]
                if (!event.intersects(column.last())) {
                    column.add(event)
                    placed = true
                    break
                }
            }

            if (!placed) {
                val newList = ArrayList<CollisionEventWrapper>()
                newList.add(event)

                columns.add(newList)
            }

            if (lastEventEnding == null || event.end() > lastEventEnding) {
                lastEventEnding = event.end()
            }
        }

        if (columns.size > 0) {
            packEvents(columns, width)
        }

        return events.fold(listOf()) { acc, collisionEventWrapper ->
            acc + collisionEventWrapper.event
        }
    }


    private fun packEvents(columns: ArrayList<ArrayList<CollisionEventWrapper>>, width: Int) {
        for (i in 0 until columns.size) {
            val column = columns[i]
            for (j in 0 until column.size) {
                val event = column[j]
                val colSpan = expandEvent(event, i, columns)

                event.event.x = (i * width / columns.size).toFloat()
                event.event.width = (width * colSpan / columns.size).toInt()
            }
        }
    }

    private fun expandEvent(event: CollisionEventWrapper, col: Int, columns: ArrayList<ArrayList<CollisionEventWrapper>>) : Float{
        var colSpan = 1.0f

        for (i in col + 1 until columns.size) {
            val column = columns[i]
            for (current in column) {
                event.column = i
                if (event.intersects(current)) {
                    return colSpan
                }
            }
            ++colSpan
        }

        return colSpan
    }


    class CollisionEventWrapper(val event: EventWrapper) : Comparable<CollisionEventWrapper> {
        var column: Int = -1

        override fun compareTo(other: CollisionEventWrapper): Int {
            return column.compareTo(other.column)
        }

        fun intersects(other: CollisionEventWrapper) : Boolean =
            begin() >= other.begin() && begin() <= other.end()
                    || end() >= other.begin() && end() <= other.end()
                    || other.begin() >= begin() && other.begin() <= end()
                    || other.end() >= begin() && other.end() <= end()

        fun begin() = event.begin()

        fun end(): Date = event.end()
    }
}