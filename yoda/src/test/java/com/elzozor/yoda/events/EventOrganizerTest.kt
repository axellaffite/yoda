package com.elzozor.yoda.events

import com.elzozor.yoda.events.utility.FakeEventWrapper
import com.elzozor.yoda.utils.DateExtensions.setup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.util.*

internal class EventOrganizerTest {

    /**
     * This function test the events in the following
     * configuration :
     *       |-------| event 0
     *       |-------| event 1
     *          |--------| event 2       |----| event 3
     *   |------|        |------| |------|
     *    event 4         event 5  event 6
     * --10h----------------------11h------------->
     */
    @Test
    fun organize() {
        val event0 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,10),
            Date().setup(2020, Calendar.JANUARY, 0, 10, 30),
            false
        )

        val event1 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,10),
            Date().setup(2020, Calendar.JANUARY, 0, 10, 30),
            false
        )

        val event2 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,20),
            Date().setup(2020, Calendar.JANUARY, 0, 10, 40),
            false
        )

        val event3 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 11, 0),
            Date().setup(2020, Calendar.JANUARY, 0, 11, 5),
            false
        )

        val event4 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,0),
            Date().setup(2020, Calendar.JANUARY, 0, 10, 20),
            false
        )

        val event5 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,40),
            Date().setup(2020, Calendar.JANUARY, 0, 10, 50),
            false
        )

        val event6 = FakeEventWrapper(
            Date().setup(2020, Calendar.JANUARY, 0, 10,55),
            Date().setup(2020, Calendar.JANUARY, 0, 11, 0),
            false
        )

        val events = listOf(event4, event0, event1, event2, event5, event6, event3)

        val organized = EventOrganizer(events).organize(1000)
        assertEquals(7, organized.size)

        val event01 = organized.subList(1,3)
        assertSame(event4, organized[0])
        assert(event0 in event01)
        assert(event1 in event01)
        assertSame(event2, organized[3])
        assertSame(event5, organized[4])
        assertSame(event6, organized[5])
        assertSame(event3, organized[6])
    }

}