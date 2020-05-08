package com.elzozor.timetabledisplayer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.elzozor.yoda.events.EventWrapper
import com.google.android.material.color.MaterialColors
import kotlinx.android.synthetic.main.yoda_tester_fragment.*
import org.koin.android.architecture.ext.viewModel
import java.util.*
import java.util.Calendar.HOUR
import java.util.Calendar.HOUR_OF_DAY


class YodaTester : Fragment() {

    companion object {
        fun newInstance() = YodaTester()
    }

    private val model : YodaTesterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.yoda_tester_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        button2.setOnClickListener { generateRandomEvents() }
    }

    private fun generateRandomEvents() {
        var events = listOf<Event>()
        for (i in 0..10) {
            events = events + randomEvent()
        }

        day_yoda.setViewBuilder { context, event, x, y, width, height ->
            Pair(true, EventCardView(context).apply {
                setBackgroundColor(Color.parseColor(randomColor()))
                setEvent(event as Event)

                val params = RelativeLayout.LayoutParams(width, height)
                params.leftMargin = x
                params.topMargin = y

                layoutParams = params
            })
        }

        day_yoda.setEvents(viewLifecycleOwner.lifecycleScope, events)
    }

    private fun randomEvent() : Event {
        val rand = Random()
        return createEvent(rand.nextInt(10) + 5, rand.nextInt(60), rand.nextInt(3))
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
