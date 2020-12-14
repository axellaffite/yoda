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
        this.events = events
            .map { CollisionEventWrapper(it) }
            .sortedWith { eventA, eventB ->
                var comp = eventA.begin().compareTo(eventB.begin())
                if (comp == 0) {
                    comp = eventB.end().compareTo(eventA.end())
                }

                comp
            }
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

        for (event in events) {
            // If the current event is after all the previous
            // events ( it begin after the maximum end )
            // we can pack them as they are a group
            // which will never intersects an other one.
            lastEventEnding?.takeIf { event.begin() > it }?.let {
                packEvents(columns, width)
                columns.clear()
                lastEventEnding = null
            }

            // We insert the event in the first column
            // where it doesn't intersect an other event
            var placed = false
            for (column in columns) {
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
                columns.add(arrayListOf(event))
            }

            // We finally update the lastEventEnding
            // to maintain it as the maximum of all the
            // events.end() values.
            if (lastEventEnding == null || event.end() > lastEventEnding) {
                lastEventEnding = event.end()
            }
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
        for ((index, actualColumn) in columns.withIndex()) {
            for (eventWrapper in actualColumn) {
                val colSpan = expandEvent(eventWrapper, index, columns)

                eventWrapper.event.x = ((index * width).toFloat() / columns.size.toFloat())
                eventWrapper.event.width = (width * colSpan / columns.size.toFloat()).toInt()
            }
        }
    }


    /**
     * This function expand the event until
     * it intersects an another event.
     *
     * @param eventToExpand The event to expand
     * @param columnIndex The current column of the event
     * @param columns The events packed into columns
     * @return The event width in columns
     */
    private fun expandEvent(
        eventToExpand: CollisionEventWrapper,
        columnIndex: Int,
        columns: ArrayList<ArrayList<CollisionEventWrapper>>
    ) : Float{
        var colSpan = 1.0f

        val remainingColumns = columns.subList(columnIndex + 1, columns.size)
        for ((index, column) in remainingColumns.withIndex()) {
            for (event in column) {
                eventToExpand.column = index
                if (eventToExpand.intersects(event)) {
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