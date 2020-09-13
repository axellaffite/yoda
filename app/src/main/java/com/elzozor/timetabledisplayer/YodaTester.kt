package com.elzozor.timetabledisplayer

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.elzozor.yoda.Day
import com.elzozor.yoda.events.EventWrapper
import kotlinx.android.synthetic.main.yoda_tester_fragment.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.koin.android.architecture.ext.viewModel
import java.util.*
import java.util.Calendar.HOUR


class YodaTester : Fragment() {

    companion object {
        fun newInstance() = YodaTester()
    }

    private val model: YodaTesterViewModel by viewModel()

    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.yoda_tester_fragment, container, false)
    }

    private fun alldaybuilder(eventList: List<EventWrapper>): View {
        return LinearLayout(context).apply {
            eventList.forEach { event ->
                addView(
                    EventCardView(context).apply {
                        setBackgroundColor(Color.parseColor(randomColor()))
                        setEvent(event as Event)

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                )
            }

            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun daybuilder(context: Context, event: EventWrapper, x: Int, y: Int, width: Int, height: Int) : Pair<Boolean, View> {
        return Pair(false, EventCardView(context).apply {
            setEvent(event as Event)
            setCardBackgroundColor(Color.parseColor(randomColor()))
        })
    }

    private fun emptyDayBuilder() = TextView(context).apply {
        gravity = Gravity.CENTER
        text = "Nothing to show today !"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_week.setOnClickListener { generateWeek() }
        button_empty_day.setOnClickListener { generateEmptyDay() }
        button_classic_day.setOnClickListener { generateClassicDay() }
        button_classic_and_all_day.setOnClickListener { generateClassicAndAllDayDay() }

        day_yoda.apply {
            dayBuilder = this@YodaTester::daybuilder

            allDayBuilder = this@YodaTester::alldaybuilder

            emptyDayBuilder = this@YodaTester::emptyDayBuilder
        }

        week.apply {
            dayBuilder = this@YodaTester::daybuilder

            allDayBuilder = this@YodaTester::alldaybuilder

            emptyDayBuilder = this@YodaTester::emptyDayBuilder

            fit = Day.Fit.BOUNDS_ADAPTIVE
            hoursMode = Day.HoursMode.COMPLETE
            displayMode = Day.Display.FIT_TO_CONTAINER
            start = 7
            end = 20
        }
    }

    private fun generateEmptyDay() {
        job?.cancel()
        job = lifecycleScope.launchWhenResumed {
            day_yoda.setEvents(listOf(), tester_main.height, tester_main.width)
            withContext(Main) {
                day_yoda.visibility = VISIBLE
                week.visibility = GONE
            }
        }
    }

    private fun generateClassicDay() {
        job?.cancel()

        job = lifecycleScope.launchWhenResumed {

            val events = withContext(Default) {
                (0..10).map {
                    randomEvent()
                }.filter { !it.isAllDay() }
            }

            day_yoda.setEvents(events, tester_main.height, tester_main.width)
            withContext(Main) {
                day_yoda.visibility = VISIBLE
                week.visibility = GONE
            }
        }
    }

    private fun generateClassicAndAllDayDay() {
        job?.cancel()

        job = lifecycleScope.launchWhenResumed {

            val events = withContext(Default) {
                (0..10).map {
                    randomEvent()
                } + (0..10).map {
                    randomEventAllDay()
                }
            }

            day_yoda.setEvents(events, tester_main.height, tester_main.width)
            withContext(Main) {
                day_yoda.visibility = VISIBLE
                week.visibility = GONE
            }
        }
    }

    private fun generateWeek() {
        job?.cancel()

        suspend fun generateDay(year: Int, month: Int, day: Int) = withContext(Default) {
            (0..6).map { randomEvent(year, month, day) }
        }

        job = lifecycleScope.launchWhenResumed {
            val events =
                (0..0).mapIndexed { index, _ ->
                    generateDay(2019, 12, 21 + index)
                }.flatten()

            val start = events.minOfOrNull { it.begin }!!
            week.setEvents(start, events, tester_main.height, tester_main.width)
            withContext(Main) {
                day_yoda.visibility = GONE
                week.visibility = VISIBLE
            }
        }
    }

    private fun randomEvent(year: Int = 2019, month: Int = 12, day: Int = 21) : Event {
        val rand = Random()
        return createEvent(year, month, day,rand.nextInt(10) + 5, rand.nextInt(60), rand.nextInt(3))
    }

    private fun randomEventAllDay(year: Int = 2019, month: Int = 12, day: Int = 21) : Event {
        val rand = Random()
        return createEvent(year, month, day,rand.nextInt(10) + 5, rand.nextInt(60), 0)
    }


    private fun createEvent(year: Int, month: Int, day: Int, hour: Int, minute: Int, time : Int) : Event {
        val c = Calendar.getInstance()
        c.set(year, month, day, hour, minute, 0)
        val deb = c.time
        c.add(HOUR, time)
        val fin = c.time

        return Event(
            deb,
            fin,
            randomTitle(),
            randomDescription()
        )
    }

    private fun randomTitle() : String {
        return arrayOf("Math", "Chemics", "Biology", "Geography", "Kotlin course").random()
    }

    private fun randomDescription() : String {
        return arrayOf("Do my homeworks", "Sleep", "Just work").random()
    }

    private fun randomColor() : String {
        return arrayOf(
            "#FF283593",
            "#FF1565C0",
            "#FF4E342E",
            "#FF37474F",
            "#FFD84315"
        ).random()
    }
}
