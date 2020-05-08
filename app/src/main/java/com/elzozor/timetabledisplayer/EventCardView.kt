package com.elzozor.timetabledisplayer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.elzozor.yoda.events.EventWrapper
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.event.view.*

class EventCardView(context: Context,
                    attrs: AttributeSet?): FrameLayout(context, attrs) {


    constructor(context: Context,
                attrs: AttributeSet?,
                event: EventWrapper) : this(context, attrs) {
        this.event = event
    }

    constructor(context: Context) : this(context, null)

    lateinit var event: EventWrapper

    init {
        inflate(context, R.layout.event, this)
    }

    fun setEvent(event: Event) : EventCardView {
        this.event = event
        title.text = event.title
        return this
    }
}