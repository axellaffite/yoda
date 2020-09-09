package com.elzozor.yoda

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.elzozor.yoda.events.EventOrganizer
import com.elzozor.yoda.events.EventWrapper
import com.elzozor.yoda.utils.DateExtensions.get
import com.elzozor.yoda.utils.TimeUtils.Companion.millisecondsToHours
import com.elzozor.yoda.utils.TimeUtils.Companion.onlyTimeAsMs
import com.elzozor.yoda.utils.TimeUtils.Companion.rangeBetweenDates
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*
import java.util.Calendar.HOUR_OF_DAY

class Day(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    /**
     * Defines the display mode.
     * - EXPAND means that the view will expand until
     *   all the events are displayed with the
     *   default hoursHeight size ( 48dp per hour )
     * - FIT_TO_CONTAINER means that the view
     *   will fit to the size which is provided
     *   in the setEvents() function.
     */
    enum class Display { EXPAND, FIT_TO_CONTAINER }

    /**
    * Defines the fit mode.
    * - AUTO means that the Day hours will fit
    *   the events hours. For example if the first
    *   event starts at 8am and the last one ends at
    *   10am, the start variable will be set to 8 and
    *   the end variable will be set to 11. (end hours + 1).
    */
    enum class Fit { AUTO, BOUNDS_ADAPTIVE, BOUNDS_STRICT }

    /**
     * Defines how the hours are displayed.
     * The formats are the following :
     *  - SIMPLE : 00 -> 23
     *  - SIMPLE_SHORT : 0 -> 23
     *  - COMPLETE : 00:00 -> 23:00
     *  - COMPLETE_SHORT : 0:00 -> 23:00
     *  - COMPLETE_H : 00h00 -> 23h00
     *  - COMPLETE_H_SHORT : 0h00 -> 23h00
     */
    enum class HoursMode { NONE, SIMPLE, SIMPLE_SHORT, COMPLETE, COMPLETE_SHORT, COMPLETE_H, COMPLETE_H_SHORT }

    /**
     * Defines the height of the hours.
     * Can be set in the xml.
     */
    var hoursMode = HoursMode.COMPLETE

    /**
    * Defines the height of the hours
    * Is updated when the updateHoursHeight function
    * is called.
    */
    private var hourHeight = 0

    /**
    * These two variable defines the start
    * and end hours of the Day view.
    */
    var start: Int
    var end: Int

    /**
    * Defines the display mode.
    * See the enum class Display documentation
    * above.
    */
    var displayMode = Display.EXPAND

    /**
    * Defines the fit mode.
    * Se tge enum class Fit
    * documentation above.
    */
    var fit = Fit.AUTO

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

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Day,
            0, 0
        ).apply {

            try {
                start = getInteger(R.styleable.Day_start, 0).coerceAtLeast(0)
                end = getInteger(R.styleable.Day_end, 24).coerceAtMost(24)
                displayMode = Display.values()[getInt(R.styleable.Day_displayMode,  0)]
                hoursMode = HoursMode.values()[getInt(R.styleable.Day_hoursFormat, 2)]
                fit = Fit.values()[getInt(R.styleable.Day_fit, 0)]
            } finally {
                recycle()
            }
        }

        inflate(context, R.layout.day, this)
    }


    /**
     * To use this function you must have provided the
     * view builder functions that are :
     *  - dayBuilder
     *  - allDayBuilder
     *  - emptyDayBuilder
     *
     * See their documentation for more information.
     * You set them directly by assigning them a value.
     *
     * This function will arrange the events in such a
     * way that they fill the whole screen width.
     *
     * The function is asynchronous and use
     * the kotlin coroutines to perform that.
     *
     * @param events
     */
    suspend fun setEvents(events: List<EventWrapper>, containerHeight: Int, width: Int) =
        organizeEventsInBackground(events, containerHeight, width)


    /**
     * Provide a function that organize the
     * given events on a background thread.
     *
     * @param events The events to organize
     */
    private suspend fun organizeEventsInBackground(events: List<EventWrapper>, containerHeight: Int, width: Int) =
        withContext(Default) {
            checkHoursFit(events)
            updateHourHeight(containerHeight)

            val hourWidth = width / 10
            val eventsWidth =
                if (hoursMode == HoursMode.NONE) { width }
                else { width - hourWidth }

            val heightOffset = start * hourHeight

            val allDayEvents = events.filter { it.isAllDay() }
            val organizedEvents = organizeEvents(events.filter { !it.isAllDay() }, eventsWidth)

            clearViews()
            addEventsToView(allDayEvents, organizedEvents, hourWidth, heightOffset)
        }


    /**
     * If the autoFithours var is set to true,
     * this function will automatically fits
     * the starting and ending hours to the
     * events bounds.
     *
     * @param events The events list you want to plot
     */
    private suspend fun checkHoursFit(events: List<EventWrapper>) = withContext(Default) {
        val startDate = events.map { it.begin() }.minOrNull()
        val endDate = events.map { it.end() }.maxOrNull()

        when (fit) {
            Fit.AUTO -> {
                startDate?.get(HOUR_OF_DAY)?.let {
                    start = it
                }

                endDate?.get(HOUR_OF_DAY)?.let {
                    end = it + 1
                }
            }

            Fit.BOUNDS_ADAPTIVE -> {
                startDate?.get(HOUR_OF_DAY)?.let {
                    start = start.coerceAtMost(it)
                }

                endDate?.get(HOUR_OF_DAY)?.let {
                    end = end.coerceAtLeast(it + 1)
                }
            }

            else -> {}
        }
    }

    private fun updateHourHeight(containerHeight: Int) {
        hourHeight = when (displayMode) {
            Display.EXPAND -> resources.getDimension(R.dimen.hour_height).toInt()
            Display.FIT_TO_CONTAINER -> containerHeight / (end - start)
        }
    }


    /**
     * Organize the events and return the
     * final result.
     *
     * @param events The events to orgnanize
     * @param screenWidth The total container width
     * @return The organized events
     */
    private suspend fun organizeEvents(events: List<EventWrapper>, screenWidth: Int) =
        withContext(Default) {
            val cal = Calendar.getInstance()

            EventOrganizer(events.filter {
                cal.time = it.begin()
                val hourBeg = cal.get(HOUR_OF_DAY)
                cal.time = it.end()
                val hourEnd = cal.get(HOUR_OF_DAY)

                hourBeg >= start && hourEnd <= end && hourBeg < hourEnd
            }).organize(screenWidth)
        }


    /**
     * Clear the day container view.
     */
    private suspend fun clearViews() = withContext(Main) {
        day_container.removeAllViews()
        all_day_container.removeAllViews()
        empty_day.removeAllViews()
    }


    /**
     * Add all the events to the day_container view.
     *
     * @param organizedEvents The organized events
     * @param hourWidth The total width taken by the hours
     * @param heightOffset The offset that is non null if \
     * the starting hour is greater than 0.
     */
    private suspend fun addEventsToView (
        allDayEvents: List<EventWrapper>,
        organizedEvents: List<EventWrapper>,
        hourWidth: Int,
        heightOffset: Int
    ) {
        setViewsVisibility(allDayEvents.isNotEmpty(), organizedEvents.isNotEmpty())

        if (allDayEvents.isEmpty() && organizedEvents.isEmpty()) {
            setupEmptyDayView()
        } else {
            if (allDayEvents.isNotEmpty()) {
                setupAllDayView(allDayEvents)
            }

            if (organizedEvents.isNotEmpty()) {
                setupDayView(organizedEvents, hourWidth, heightOffset)
            }
        }
    }

    /**
     * This function is in charge to display the
     * view that is shown when the event list
     * is empty.
     */
    private suspend fun setupEmptyDayView() {
        addViewOnMainThread(empty_day, emptyDayBuilder())
    }

    /**
     * This function is in charge to display to view
     * that is shown where there are events that are
     * considered as "all day".
     *
     * @param allDayEvents The events that are considered
     * as "all day"
     */
    private suspend fun setupAllDayView(allDayEvents: List<EventWrapper>) {
        addViewOnMainThread(all_day_container, allDayBuilder(allDayEvents))
    }

    /**
     * This function is in charge to build
     * the day view which is where the "classic"
     * events (which are not considered as "all day")
     * are displayed.
     *
     * This function build the view into a RelativeLayout which is
     * constructed in this function.
     * Once done, the RelativeLayout is added into the day_container
     * view.
     *
     * It's done this way to avoid events to be drawn one by one
     * and to increase speed.
     *
     * @param organizedEvents The events to display
     * @param hourWidth The width occupied by the hours
     * @param heightOffset The offset that is non null if \
     * the starting hour is greater than 0.
     */
    private suspend fun setupDayView(organizedEvents: List<EventWrapper>,
                                     hourWidth: Int,
                                     heightOffset: Int)
    {
        val dayView = RelativeLayout(context).apply {
            layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        organizedEvents.forEach { event ->
            val ey = computeEventPosition(event) - heightOffset
            val ex = event.x.toInt() + hourWidth
            val eheight = computeEventHeight(event)
            val ewidth = event.width

            val result = dayBuilder(
                context, event,
                ex, ey, ewidth, eheight
            )

            val constraintsSet = result.first
            val eventView = result.second

            if (!constraintsSet) {
                eventView.apply {
                    val params = RelativeLayout.LayoutParams(ewidth, eheight)
                    params.leftMargin = ex
                    params.topMargin = ey

                    layoutParams = params
                }
            }

            addViewOnMainThread(dayView, eventView)
        }

        addViewOnMainThread(day_container, dayView)
        addHoursToView(hourWidth, hourHeight, heightOffset)
    }

    /**
     * Add the hours to the day_container view.
     *
     * @param hourWidth The total width taken by the hours
     * @param hourHeight The total height taken by the hours
     * @param heightOffset The offset that is non null if \
     * the starting hour is greater than 0.
     */
    private suspend fun addHoursToView(hourWidth: Int, hourHeight: Int, heightOffset: Int) =
        withContext(Default) {
            if (hoursMode == HoursMode.NONE) {
                return@withContext
            }

            val hourText = getHoursFormat()
            for (hour in start until end) {
                addViewOnMainThread(day_container, TextView(context).apply {
                    text = hourText.format(hour)
                    layoutParams = RelativeLayout.LayoutParams(hourWidth, hourHeight).apply {
                        this.topMargin = (hour * hourHeight) - heightOffset
                        this.leftMargin = 0
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                })
            }
        }

    @Throws(IllegalStateException::class)
    private fun getHoursFormat(): String {
        val res = when (hoursMode) {
            HoursMode.SIMPLE -> R.string.hours_simple
            HoursMode.SIMPLE_SHORT -> R.string.hours_simple_short
            HoursMode.COMPLETE -> R.string.hours_complete
            HoursMode.COMPLETE_SHORT -> R.string.hours_complete_h_short
            HoursMode.COMPLETE_H -> R.string.hours_complete_h
            HoursMode.COMPLETE_H_SHORT -> R.string.hours_complete_h_short
            HoursMode.NONE -> throw IllegalStateException("Hours are disabled")
        }

        return context.getString(res)
    }

    private suspend fun setViewsVisibility(isAllDayVisible: Boolean,
                                   isDayVisible: Boolean,
                                   isEmptyDayVisible: Boolean = !(isAllDayVisible || isDayVisible)
    ) {
        val convertVisibility = { isVisible: Boolean -> if (isVisible) VISIBLE else GONE }

        withContext(Main) {
            day_container.visibility = convertVisibility(isDayVisible)
            all_day_container.visibility = convertVisibility(isAllDayVisible)
            empty_day.visibility = convertVisibility(isEmptyDayVisible)
        }
    }

    /**
     * Add a view to the container
     * view on main thread.
     *
     * @param container The container view
     * @param child The view to add
     */
    private suspend fun addViewOnMainThread(container: ViewGroup, child: View) =
        withContext(Main) {
            container.addView(child)
        }


    /**
     * Computes the event position given
     * by the begin of the event.
     * This height will be relative to the
     * starting hour of the day.
     *
     * @param event
     * @return
     */
    private fun computeEventPosition(event: EventWrapper) =
        (millisecondsToHours(onlyTimeAsMs(event.begin())) * hourHeight).toInt()


    private fun computeEventHeight(event: EventWrapper) =
        (millisecondsToHours(rangeBetweenDates(event.begin(), event.end())) * hourHeight).toInt()
}


