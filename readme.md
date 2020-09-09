# Yoda

[![](https://jitpack.io/v/ElZozor/yoda.svg)](https://jitpack.io/#ElZozor/yoda)

## Example

My other application is made with this library : https://github.com/ElZozor/ut3_calendar  
<img src="https://raw.githubusercontent.com/ElZozor/yoda/master/example/schedule.jpg" height="600">

## Gradle implementation

__Step 1.__ Add the Jitpack repository to you build file :
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

__Step 2.__ Add the Yoda dependency to your project Gradle file :

You must replace `version_number` with the version provided above.
```
implementation 'com.github.ElZozor:yoda:version_number'
```

## Abstract
This library provides functionalities to display a timetable.  
Let's see together how to use it !

#### Step 1 - Implementation

Ensure you have implemented the library correctly.

#### Step 2 - Creating the layout

You must create a layout where you'll store the Day view.  
The Day view will expand to fit it's content so you may want to include it into a ScrollView.

```xml
[...]

<ScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    <com.elzozor.yoda.Day
        android:id="@+id/day_yoda"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

</ScrollView>

[...]
```

There a 4 options that are available on the Day component :
 - __start__ : Defines the start hour of the Day
 - __end__ : Defines the start hour of the Day
 - __fit__ : Defines how the Day will fit the Events hours
 - __display__ : Defines how the Day will expand its height
 - __hoursFormat__ : Defines how the hours are displayed

##### Explanations : Fit  
Fit defines how the Day component will adapt its hours to the Event ones.
There are 3 options available :
 - __AUTO__ : Will fit the hours to the Event ones. For example if the earlier hour is 8 and the latest is 10, the Day hours will start at 8 and end at 11 (end + 1).
 - __BOUNDS_ADAPTIVE__ : Will fit the hours to the parameters start and end but will expand them if there are Events that are not in that bound.
 - __BOUNDS_STRICT__ : Will fit the hours to the parameters start and end not matter if there are Events that do not fit in it.

#### Step 3 - Event view

Once you've created the layout that will store your events, you should provide a view that will display them.  
The Yoda library is actually made in such a way that it provides functionalities to find the proper events positions but you must provide a way to show them.  
So go ahead and create yours !

Here is a very basic example :

```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/EventCardView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:gravity="center"
    android:textColor="@android:color/white"
    android:textSize="14sp"
    android:textStyle="bold" />
```

And here is the class associated with it :

```kotlin
[A package]
[Some imports]

class EventCardView(context: Context,
                    attrs: AttributeSet?)
                    : TextView(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.event, this)
    }

    fun setEvent(event: Event) {
        this.event = event
        setText(event.title)
    }
}
```

#### Step 4 - Extends EventWrapper

As you may have seen, the setEvents function takes two parameters, a `LifecycleCoroutineScope` and a list of EventWrapper.  
We will get into the first parameter later, for now, let's focus on the second one.  

As it takes an EventWrapper, you must provide a class that extends this one.  
Three functions must be overriden as they are abstract.  

Here is an example :

```kotlin
[A package]
[Some imports]

data class Event (
    val begin : Date,
    val end : Date,
    val title : String,
    val description : String) : EventWrapper() 
{

    override fun begin() = begin

    override fun end() = end

    override fun isAllDay() = (begin == end)
}
```

You can now simply pass a list of your Event class to the `setEvents` function of the Day layout !


#### Step 5 - Use the Day layout !

You must provide a way for the Day layout to build the views, otherwise it will throw an Exception.  
You do it by assigning the variables to a value.

```kotlin
lateinit var dayBuilder: (Context, EventWrapper, Int, Int, Int, Int) -> Pair<Boolean, View>
```

This function defines how the Events views must be constructed.
The parameters are :
- A context given by the function
- An EventWrapper that contains the current
  event to build
- x, y, w, h which are the position and size
  of the EventView.

The return of this function is a Pair that contains
in first argument if the constraints have been set
on the returned View ( position and size ) and in
second argument the constructed view.

```kotlin
lateinit var allDayBuilder: (List<EventWrapper>) -> View
```

This function constructs the "all day view" where are display the events that are affiliated to the entire day and not only to a specific time.
Put another way, there are the EventWrappers that returns true to the `isAllDay()` function.

```kotlin
lateinit var emptyDayBuilder: () -> View
```
This function simply construct the view that is displayed when the day is empty.
Simply do want you want !

Here are some examples :

```kotlin
// day_yoda is the view that we are accessing by it's id.

// Here is an exemple where we don't set the layout params
day_yoda.dayBuilder = { context, event, x, y, width, height ->
    Pair(false, EventCardView(context).apply {
        setBackgroundColor(Color.parseColor(randomColor()))
        setEvent(event as Event)
    })
}

// And here is an exemple where we set the layout params
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


// Creates a vertical LinearLayout that
// contains multiple EventCardViews
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

// Simply display a message to say
// that the day is empty.
day_yoda.emptyDayBuilder = {
    TextView(context).apply {
        gravity = Gravity.CENTER
        text = "Nothing to show today !"
    }
}

```

You can now use the `setEvents` function just like this :
```kotlin
lifecycleScope.launchWhenResumed {
  day_yoda.setEvents(myEventList, container_view.height)
}
```

The Yoda library will now display the events into the Day layout in a background thread which will stop when your application is paused.  

The viewLifecycleOwner.lifecycleScope is the lifecycleScope of you fragment view.  
