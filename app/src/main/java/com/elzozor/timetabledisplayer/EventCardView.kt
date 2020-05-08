package com.elzozor.timetabledisplayer

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.elzozor.yoda.events.EventWrapper
import kotlinx.android.synthetic.main.event.view.*

class EventCardView(context: Context,
                    attrs: AttributeSet?): CardView(context, attrs) {


    constructor(context: Context,
                attrs: AttributeSet?,
                event: EventWrapper) : this(context, attrs) {
        this.event = event
    }

    constructor(context: Context) : this(context, null)

    lateinit var event: EventWrapper

    init {

        cardElevation = 0.0f
        inflate(context, R.layout.event, this)
    }

    fun setEvent(event: Event) : EventCardView {
        this.event = event
        title.text = event.title
        return this
    }
}