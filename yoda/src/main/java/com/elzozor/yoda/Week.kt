package com.elzozor.yoda

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.elzozor.yoda.events.EventWrapper
import com.elzozor.yoda.utils.DateExtensions.add
import com.elzozor.yoda.utils.DateExtensions.get
import com.elzozor.yoda.utils.DateExtensions.resetTime
import java.util.*

class Week(context: Context, attrs: AttributeSet?): RelativeLayout(context, attrs) {

    /**
     * Defines the height of the hours.
     * Can be set in the xml.
     */
    var hoursMode = Day.HoursMode.COMPLETE

    /**
     * These two variable defines the start
     * and end hours of the Day view.
     */
    var start: Int = 7
    var end: Int = 20

    /**
     * Defines the display mode.
     * See the enum class Display documentation
     * above.
     */
    var displayMode = Day.Display.FIT_TO_CONTAINER

    /**
     * Defines the fit mode.
     * Se tge enum class Fit
     * documentation above.
     */
    val fit = Day.Fit.BOUNDS_STRICT

    /**
     * This function defines how the Events views
     * must be constructed.
     * The parameters are :
     * - A context given by the function
     * - An EventWrapper that contains the current
     *   event to build
     * - x, y, w, h which are the position and size
     *   of the EventView.
     *
     * The return of this function is a Pair that contains
     * in first argument if the constraints have been set
     * on the returned View ( position and size ) and in
     * second argument the constructed view.
     */
    lateinit var dayBuilder: (Context, EventWrapper, Int, Int, Int, Int) -> Pair<Boolean, View>

    /**
     * This function constructs the "all day view" where
     * are display the events that are affiliated to the entire
     * day and not only to a specific time.
     *
     * Put another way, there are the EventWrappers that returns
     * true to the isAllDay() function.
     */
    lateinit var allDayBuilder: (List<EventWrapper>) -> View

    /**
     * This function simply construct the view
     * that is displayed when the day is empty.
     *
     * Simply do want you want !
     */
    lateinit var emptyDayBuilder: () -> View




    suspend fun setEvents(from: Date, events: List<EventWrapper>, height: Int, width: Int) {
        val fromClean = from.resetTime()
        val dayCount = when {
            events.any { it.begin() > fromClean.add(Calendar.DAY_OF_YEAR, 6) && it.begin() < fromClean.add(Calendar.DAY_OF_YEAR, 7)} -> {
                7
            }

            events.any { it.begin() > fromClean.add(Calendar.DAY_OF_YEAR, 5) && it.begin() < fromClean.add(Calendar.DAY_OF_YEAR, 6) } -> {
                6
            }

            else -> 5
        }

        val days = (0 until dayCount).map { Day(context, null) }

        val hourWidth = computeHourPlace()
        val dayWidth = (width - hourWidth) / dayCount
        var min = events.map { it.begin().get(Calendar.HOUR_OF_DAY) }.minOrNull() ?: 7
        var max = events.map { it.end().get(Calendar.HOUR_OF_DAY) + 1 }.maxOrNull() ?: 20

        min = min.coerceAtMost(start)
        max = max.coerceAtLeast(end)

        days.forEachIndexed { index, day ->
            day.dayBuilder = dayBuilder
            day.allDayBuilder = allDayBuilder
            day.emptyDayBuilder = emptyDayBuilder

            day.hoursMode = Day.HoursMode.NONE
            day.start = min
            day.end = max
            day.displayMode = displayMode
            day.fit = Day.Fit.BOUNDS_STRICT
            day.x = hourWidth + (dayWidth * index)
            day.y = 0f
            day.layoutParams = LayoutParams(width, height)


            val currentDate = fromClean.add(Calendar.DAY_OF_YEAR, index)
            val dayEvents = events.filter {
                it.begin() > currentDate
                && it.end() < currentDate.add(Calendar.DAY_OF_YEAR, 1)
            }

            day.setEvents(dayEvents, height, dayWidth.toInt())
        }

        removeAllViews()
        generateHours(min, max, height, hourWidth.toInt())
        days.forEach { addView(it) }
    }

    private fun computeHourPlace(): Float {
        val resID = getHourResource()

        val text = resID?.run {
            context.getString(resID).format(23,59)
        } ?: ""

        return TextView(context).paint.measureText(text)
    }

    private fun generateHours(start: Int, end: Int, height: Int, hourWidth: Int) {
        val res = getHourResource()
        res?.also {
            val textFormat = context.getString(it)
            val hourHeight = (height / (end - start)).toFloat()

            (start..end).forEachIndexed { index, hour ->
                addView(
                    TextView(context).apply {
                        text = textFormat.format(hour, 0)
                        x = 0f
                        y = index * hourHeight
                        layoutParams = LayoutParams(hourWidth, hourHeight.toInt())
                    }
                )
            }
        }
    }

    private fun getHourResource() = when (hoursMode) {
        Day.HoursMode.NONE -> null
        Day.HoursMode.SIMPLE -> R.string.hours_simple
        Day.HoursMode.SIMPLE_SHORT -> R.string.hours_simple_short
        Day.HoursMode.COMPLETE -> R.string.hours_complete
        Day.HoursMode.COMPLETE_SHORT -> R.string.hours_complete_short
        Day.HoursMode.COMPLETE_H -> R.string.hours_complete_h
        Day.HoursMode.COMPLETE_H_SHORT -> R.string.hours_complete_h_short
    }

}