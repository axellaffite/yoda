package com.elzozor.yoda.events

import com.elzozor.yoda.events.utility.FakeEventWrapper
import com.elzozor.yoda.utils.DateExtensions.setup
import org.junit.Assert
import org.junit.Test
import java.util.*

internal class CollisionEventWrapper {

    @Test
    fun intersectSeconds() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10, 59, 59),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectSame() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev1))
    }

    @Test
    fun intersectInner() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 10, 30),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectOuter() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 9, 0),
                Date().setup(2020, Calendar.JANUARY, 0, 12, 0),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectOverlaps() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 9, 30),
                Date().setup(2020, Calendar.JANUARY, 0, 10, 30),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectOverlaps2() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10, 30),
                Date().setup(2020, Calendar.JANUARY, 0, 11, 30),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectTouch() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 11, 0),
                Date().setup(2020, Calendar.JANUARY, 0, 12, 0),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

    @Test
    fun intersectTouch2() {
        val ev1 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 10),
                Date().setup(2020, Calendar.JANUARY, 0, 11),
                false
            )
        )

        val ev2 = EventOrganizer.CollisionEventWrapper(
            FakeEventWrapper(
                Date().setup(2020, Calendar.JANUARY, 0, 9, 0),
                Date().setup(2020, Calendar.JANUARY, 0, 10, 0),
                false
            )
        )

        Assert.assertTrue(ev1.intersects(ev2))
    }

}