package com.example.bloopyblocks.ui.drawing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// what one finger stroke paints and when it buzzes
class StrokePainterTest
{
    // inside width on the medium phone, the 379 card minus 1 dp padding each side
    private val inner = 377f

    // the color the finger paints with
    private val teal = 0xFF42C5B5.toInt()

    // an empty grid and the painter under test
    private val pixels = MutableList(GRID_SQUARE_COUNT) { 0 }
    private val painter = StrokePainter()

    // a tap paints its square right away
    @Test
    fun downPaintsRightAway()
    {
        assertEquals(listOf(0), painter.down(1L, 5f, 5f, inner, pixels, teal))
    }

    // a square that's already this color gets no paint
    @Test
    fun sameColorDoesNothing()
    {
        pixels[0] = teal
        assertEquals(emptyList<Int>(), painter.down(1L, 5f, 5f, inner, pixels, teal))
    }

    // one fast swipe across the top row paints the other 7 squares
    @Test
    fun fastSwipePaintsEverySquare()
    {
        painter.down(1L, 5f, 5f, inner, pixels, teal)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), painter.move(1L, 372f, 5f, inner, pixels, teal))
    }

    // sliding off the grid pauses the stroke, coming back picks it up
    @Test
    fun slidingOffAndBackKeepsPainting()
    {
        painter.down(1L, 5f, 5f, inner, pixels, teal)
        assertEquals(emptyList<Int>(), painter.move(1L, -30f, 5f, inner, pixels, teal))
        assertEquals(listOf(1), painter.move(1L, 60f, 5f, inner, pixels, teal))
    }

    // the buzz waits 35 ms, so 34 ms is too soon and 35 ms is fine
    @Test
    fun buzzWaits35Ms()
    {
        assertTrue(painter.canTick(0L))
        assertFalse(painter.canTick(34L))
        assertTrue(painter.canTick(35L))
    }
}
