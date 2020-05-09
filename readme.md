# Yoda

[![](https://jitpack.io/v/ElZozor/yoda.svg)](https://jitpack.io/#ElZozor/yoda)

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

#### Step 3 - Event view

Once you've created the layout that will store your events, you should provide a view that will display them.  
The Yoda library is actually made in such a way that it provides functionalities to find the proper events positions but you must provide a way to show them.  
So go ahead and create yours !

Here is a very basic example :

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/EventCardView" >

    <TextView
        android:id="@+id/title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:gravity="center"
        android:textColor="@android:color/white"
        android:textSize="14sp"
        android:textStyle="bold" />

</FrameLayout>
```

And here is the class associated with it :

```kotlin
[A package]
[Some imports]

class EventCardView(context: Context,
                    attrs: AttributeSet?)
                    : FrameLayout(context, attrs) {


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

    fun setEvent(event: Event) {
        this.event = event
        setText(event.title)
    }

    fun setText(text: String) {
        title.text = text
    }
}
```

#### Step 4 - Extends EventWrapper

As you may have seen, the setEvents function takes two parameters, a `LifecycleCoroutineScope` and a list of EventWrapper.  
We will get into the first parameter later, for now, let's focus on the second one.  

As it takes an EventWrapper, you must provide a class that extends this one.  
Two functions must be overriden as they are abstract.  

Here is an example :

```kotlin
[A package]
[Some imports]

class Event (
    val begin : Date,
    val end : Date,
    val title : String,
    val description : String) 
    : EventWrapper() {

    override fun begin() = begin

    override fun end() = end
}
```

You can now simply pass a list of your Event class to the `setEvents` function of the Day layout !


#### Step 5 - Use the Day layout !

You must provide a way for the Day layout to build the events views, otherwise it will throw an Exception.  
You do it by calling the `setViewBuilder` function of the Day layout. This function takes a single parameter which is a lambda with the following signature :

```kotlin
(context: Context,
 event: EventWrapper,
 x: Int,
 y: Int,
 width: Int,
 height: Int) -> Pair<Boolean, View>)
```

As you can see, this function must return a Pair including a Boolean and a View.  
The Boolean is here to say whether or not you have set the Layout constraint (i.e: You have set the LayoutParams).  
The view is the one that the Day layout will show.

Here is an example :

```kotlin
// day_yoda is the view that we are accessing by it's id.

// Here is an exemple where we don't set the layout params
day_yoda.setViewBuilder { context, event, x, y, width, height ->
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
```

You can now use the `setEvents` function just like this :
```kotlin
day_yoda.setEvents(viewLifecycleOwner.lifecycleScope, myEventList)
```

The Yoda library will now display the events into the Day layout in a background thread which will stop when your application is paused.  

The viewLifecycleOwner.lifecycleScope is the lifecycleScope of you fragment view.  
