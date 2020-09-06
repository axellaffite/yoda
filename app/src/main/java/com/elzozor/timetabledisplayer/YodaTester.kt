package com.elzozor.timetabledisplayer

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.yoda_tester_fragment.*
import kotlinx.coroutines.Dispatchers.Default
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_empty_day.setOnClickListener { generateEmptyDay() }
        button_classic_day.setOnClickListener { generateClassicDay() }
        button_classic_and_all_day.setOnClickListener { generateClassicAndAllDayDay() }

        day_yoda.dayBuilder = { context, event, x, y, width, height ->
            Pair(false, EventCardView(context).apply {
                setEvent(event as Event)
                setCardBackgroundColor(Color.parseColor(randomColor()))
            })
        }

        day_yoda.allDayBuilder = { eventList ->
            LinearLayout(context).apply {
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

        day_yoda.emptyDayBuilder = {
            TextView(context).apply {
                gravity = Gravity.CENTER
                text = "Nothing to show today !"
            }
        }
    }

    private fun generateEmptyDay() {
        job?.cancel()
        job = lifecycleScope.launchWhenResumed {
            day_yoda.setEvents(listOf(), tester_main.height)
        }
    }

    private fun generateClassicDay() {
        job?.cancel()

        job = lifecycleScope.launchWhenResumed {

            val events = withContext(Default) {
                (0..200).map {
                    randomEvent()
                }.filter { !it.isAllDay() }
            }

            day_yoda.setEvents(events, tester_main.height)
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

            day_yoda.setEvents(events, tester_main.height)
        }
    }

    private fun randomEvent() : Event {
        val rand = Random()
        return createEvent(rand.nextInt(10) + 5, rand.nextInt(60), rand.nextInt(3))
    }

    private fun randomEventAllDay() : Event {
        val rand = Random()
        return createEvent(rand.nextInt(10) + 5, rand.nextInt(60), 0)
    }


    private fun createEvent(hour: Int, minute: Int, time : Int) : Event {
        val c = Calendar.getInstance()
        c.set(2019, 12, 21, hour, minute, 0)
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
