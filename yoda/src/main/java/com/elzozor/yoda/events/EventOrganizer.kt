package com.elzozor.yoda.events

import java.util.*
import kotlin.collections.ArrayList

class EventOrganizer (events: List<EventWrapper>) {

    private val events : List<CollisionEventWrapper>

    init {
        // Here we convert the event list into a
        // CollisionEventWrapper list to easily
        // compute collisions between them.
        // We then sort them by their start time
        // and by their end time in case if their
        // start time are the same.
        this.events = events.map { CollisionEventWrapper(it) }
            .sortedWith(Comparator { o1, o2 ->
                var comp = o1.begin().compareTo(o2.begin())
                if (comp == 0) {
                    comp = o2.end().compareTo(o1.end())
                }

                comp
            })
    }

    /**
     * This function organize the events,
     * pack them into groups, expand them to
     * their maximum width and returns the
     * result as an EventWrapper List.
     *
     * @param width The view's width
     * @return The resulting list
     */
    fun organize(width: Int) : List<EventWrapper> {
        // This will contain the events by columns.
        val columns = ArrayList<ArrayList<CollisionEventWrapper>>()
        // This will contain the maximum event end time
        // and will be updated along the loop.
        var lastEventEnding : Date? = null

        events.forEach { event ->
            // If the current event is after all the previous
            // events ( it begin after the maximum end )
            // we can pack them as they are a group
            // which will never intersects an other one.
            if (lastEventEnding != null && event.begin() >= lastEventEnding) {
                packEvents(columns, width)
                columns.clear()
                lastEventEnding = null
            }

            // We insert the event in the first column
            // where it doesn't intersect an other event
            var placed = false
            for (i in 0 until columns.size) {
                val column = columns[i]
                if (!event.intersects(column.last())) {
                    column.add(event)
                    placed = true
                    break
                }
            }

            // In no place has been found to put
            // the event, we simply create a new column
            // for it.
            if (!placed) {
                val newList = ArrayList<CollisionEventWrapper>()
                newList.add(event)

                columns.add(newList)
            }

            // We finally update the lastEventEnding
            // to maintain it as the maximum of all the
            // events.end() values.
            lastEventEnding = lastEventEnding?.coerceAtLeast(event.end()) ?: event.end()
        }

        // If there is a remaining column
        // in the columns list, we pack the
        // events in it.
        if (columns.size > 0) {
            packEvents(columns, width)
        }

        // We finally returns the resulting list as
        // an EventWrapper List.
        return events.map { it.event }
    }


    /**
     * This function expands the event as
     * they can be expanded.
     *
     * @param columns The resulting columns
     * @param width The width
     */
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


    /**
     * This function expand the event until
     * it intersects an another event.
     *
     * @param event The event to expand
     * @param col The current column of the event
     * @param columns The events packed into columns
     * @return The event width in columns
     */
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


    /**
     * A simple wrapper to compute collisions between Events.
     *
     * @property event
     */
    class CollisionEventWrapper(val event: EventWrapper) : Comparable<CollisionEventWrapper> {
        var column: Int = -1

        override fun compareTo(other: CollisionEventWrapper): Int {
            return column.compareTo(other.column)
        }

        /**
         * Simply test if 2 events intersects
         *
         * @param other The other event which maybe intersect this one
         * @return If the events intersect
         */
        fun intersects(other: CollisionEventWrapper) : Boolean =
            begin() >= other.begin() && begin() <= other.end()
                    || end() >= other.begin() && end() <= other.end()
                    || other.begin() >= begin() && other.begin() <= end()
                    || other.end() >= begin() && other.end() <= end()

        fun begin() = event.begin()

        fun end(): Date = event.end()
    }
}