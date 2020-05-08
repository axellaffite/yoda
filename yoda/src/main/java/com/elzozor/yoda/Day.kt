package com.elzozor.yoda

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.elzozor.yoda.events.EventOrganizer
import com.elzozor.yoda.events.EventWrapper
import com.elzozor.yoda.utils.TimeUtils.Companion.millisecondsToHours
import com.elzozor.yoda.utils.TimeUtils.Companion.onlyTimeAsMs
import com.elzozor.yoda.utils.TimeUtils.Companion.rangeBetweenDates
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.InvalidObjectException
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
class Day(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var hourHeight = resources.getDimension(R.dimen.hour_height).toInt()
    private var start: Int
    private var end: Int
    private var currentJob: Job? = null

    var autoFitHours = false

    private lateinit var containerBuilder: (Context, EventWrapper, Int, Int, Int, Int) -> Pair<Boolean, View>

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Day,
            0, 0
        ).apply {

            try {
                start = getInteger(R.styleable.Day_start, 0).coerceAtLeast(0)
                end = getInteger(R.styleable.Day_end, 24).coerceAtMost(24)
                autoFitHours = getBoolean(R.styleable.Day_autoFitHours, false)
            } finally {
                recycle()
            }
        }

        inflate(context, R.layout.day, this)
    }


    /**
     * This function is used to provide to the
     * event organizer a callback which will build
     * each views.
     * The params passed to the callback are a valid context,
     * the event you originaly provided, and all the constraints
     * that the view should match.
     *
     * The constraints are passed in the callback to let
     * the user do what he want with them, in this
     * case, true must be return with the view.
     *
     * Although, if you don't want to set the constraints
     * by yourself, just return false in the result.
     *
     * @param builder The view builder
     */
    fun setViewBuilder(
        builder: (context: Context, event: EventWrapper,
                  x: Int, y: Int, width: Int, height: Int)
        -> Pair<Boolean, View>)
    {
        containerBuilder = builder
    }


    /**
     * To use this function you must have provided the
     * view builder function with the "setContainerBuilder"
     * function.
     *
     * This function will arange the events in such a
     * way that they fill the entiere screen width.
     *
     * The function is asynchronous and use
     * the kotlin coroutines to perform that.
     *
     * The lifeCycleScope is required to ensure that
     * the view is created while the function perform
     * tasks in background and thus ensure that
     * the view is ready while adding event and hours to it.
     *
     * @param lifeCycleScope
     * @param events
     */
    @Throws(InvalidObjectException::class)
    fun setEvents(lifeCycleScope: LifecycleCoroutineScope, events: List<EventWrapper>) {
        if (!this::containerBuilder.isInitialized) {
            throw InvalidObjectException("You must provide a view Builder via the 'setContainerBuilder' function")
        }


        overrideExistingJob(
            lifeCycleScope.launchWhenResumed {
                organizeEventsInBackground(events)
            }
        )
    }


    /**
     * Cancel and override the existing
     * job with the new created job.
     *
     * @param newJob The new job
     */
    @Synchronized
    private fun overrideExistingJob(newJob: Job) {
        currentJob?.apply {
            if (isActive) {
                cancel()
            }
        }

        currentJob = newJob
    }

    /**
     * Provide a function that orgnanize the
     * given events on a background thread.
     *
     * @param events The events to organize
     */
    private suspend fun organizeEventsInBackground(events: List<EventWrapper>) {
        checkHoursFit(events)

        val totalWidth = day_container.width
        val hourWidth = totalWidth / 10
        val eventsWidth = totalWidth - hourWidth
        val heightOffset = start * hourHeight

        val organizedEvents = organizeEvents(events, eventsWidth)

        clearViews()
        addHoursToView(hourWidth, hourHeight, heightOffset)
        addEventsToView(organizedEvents, hourWidth, heightOffset)
    }


    /**
     * If the autoFithours var is set to true,
     * this function will automatically fits
     * the starting and ending hours to the
     * events bounds.
     *
     * @param events The events list you want to plot
     */
    private fun checkHoursFit(events: List<EventWrapper>) {
        if (autoFitHours) {
            val startHour = (events.fold(listOf()) { acc: List<Date>, ev -> acc + ev.begin() }).min()
            val endHour = (events.fold(listOf()) { acc: List<Date>, ev -> acc + ev.end() }).max()

            val cal = Calendar.getInstance()
            if (startHour is Date) {
                cal.time = startHour
                start = cal.get(HOUR_OF_DAY)
            }

            if (endHour is Date) {
                cal.time = endHour
                end = cal.get(HOUR_OF_DAY) + 1
            }
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
    private fun organizeEvents(events: List<EventWrapper>, screenWidth: Int)
            : List<EventWrapper> {

        val cal = Calendar.getInstance()

        return EventOrganizer(events.filter {
            cal.time = it.begin()
            val hourBeg = cal.get(HOUR_OF_DAY)
            cal.time = it.end()
            val hourEnd = cal.get(HOUR_OF_DAY)

            hourBeg >= start && hourEnd <= end && hourBeg < hourEnd
        }).orgnanize(screenWidth)
    }


    /**
     * Clear the day container view.
     */
    private suspend fun clearViews() {
        withContext(Main) {
            day_container.removeAllViews()
        }
    }

    /**
     * Add the hours to the day_container view.
     *
     * @param hourWidth The total width taken by the hours
     * @param hourHeight The total height taken by the hours
     * @param heightOffset The offset that is non null if \
     * the starting hour is greater than 0.
     */
    private suspend fun addHoursToView(hourWidth: Int, hourHeight: Int, heightOffset: Int) {
        withContext(Default) {
            for (hour in start until end) {
                addViewOnMainThread(
                    TextView(context).apply {
                        text = hour.toString()
                        layoutParams = LayoutParams(hourWidth, hourHeight).apply {
                            this.topMargin = (hour * hourHeight) - heightOffset
                            this.leftMargin = 0
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                    }
                )
            }
        }
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
        organizedEvents: List<EventWrapper>,
        hourWidth: Int,
        heightOffset: Int
    ) {
        organizedEvents.forEach { event ->
            val ey = computeEventPosition(event) - heightOffset
            val ex = event.x.toInt() + hourWidth
            val eheight = computeEventHeight(event)
            val ewidth = event.width

            val result = containerBuilder(
                context, event,
                ex, ey, ewidth, eheight
            )

            val constraintsSet = result.first
            val eventView = result.second

            if (!constraintsSet) {
                eventView.apply {
                    val params = LayoutParams(ewidth, eheight)
                    params.leftMargin = ex
                    params.topMargin = ey

                    layoutParams = params
                }
            }

            addViewOnMainThread(eventView)
        }
    }

    /**
     * Add a view to the day_container
     * view on main thread.
     *
     * @param view the view to add
     */
    private suspend fun addViewOnMainThread(view: View) {
        withContext(Main) {
            day_container.addView(view)
        }
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
    private fun computeEventPosition(event: EventWrapper): Int {
        return (millisecondsToHours(onlyTimeAsMs(event.begin())) * hourHeight).toInt()
    }


    private fun computeEventHeight(event: EventWrapper): Int {
        return (millisecondsToHours(rangeBetweenDates(event.begin(), event.end())) * hourHeight).toInt()
    }
}


