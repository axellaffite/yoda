package com.elzozor.timetabledisplayer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val set = mutableSetOf(
            createEvent(0, 0, 4),
            createEvent(0, 0, 1),
            createEvent(2, 0, 1),
            createEvent(0, 30, 1),
            createEvent(3, 30, 1),
            createEvent(4, 10, 1),
            createEvent(4, 10, 1),
            createEvent(4, 10, 1),
            createEvent(4, 10, 1)
        )

        day_yoda.setViewBuilder { context, event, x, y, width, height ->
            Pair(false, EventCardView(context).apply {
                setBackgroundColor(Color.parseColor(randomColor()))

                setEvent(event as Event)
                requestLayout()
            })
        }

        day_yoda.setEvents(viewLifecycleOwner.lifecycleScope, set.toList())
    }


    private fun createEvent(hour: Int, minute: Int, time : Int) : EventWrapper {
        val c = Calendar.getInstance()
        c.set(2019, 12, 21, hour, minute, 0)
        val deb = c.time
        c.add(HOUR, time)
        val fin = c.time

        return Event(
            Event(
                deb,
                fin,
                randomTitle(),
                randomDescription()
            )
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
