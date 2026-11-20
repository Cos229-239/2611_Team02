package com.example.bloopyblocks.ui.drawing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// returns the middle of a square in px, width is the grid's width
fun getSquareCenter(index: Int, width: Int): Offset
{
    val x = width * ((index % GRID_SIZE) + 0.5f) / GRID_SIZE
    val y = width * ((index / GRID_SIZE) + 0.5f) / GRID_SIZE
    return Offset(x, y)
}

// finger tests for the grid, run on the emulator
class DrawGridTest
{
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // the color the finger paints with
    private val teal = 0xFF42C5B5.toInt()

    // the grid's squares and how many strokes started and ended
    private val pixels = mutableStateListOf<Int>()
    private var strokeStarts = 0
    private var strokeEnds = 0

    // what a touch on the grid does, a test can switch it, and how many squares the grid reported
    private val touchMode = mutableIntStateOf(GRID_TOUCH_PAINT)
    private var paintCalls = 0

    // paints one square, the grid calls this
    private fun paintSquare(index: Int)
    {
        pixels[index] = teal
        paintCalls++
    }

    // counts a finger going down
    private fun startStroke()
    {
        strokeStarts++
    }

    // counts a finger coming up
    private fun endStroke()
    {
        strokeEnds++
    }

    // returns how many squares have paint
    private fun countPainted(): Int
    {
        var count = 0
        for (i in 0 until pixels.size)
        {
            if (pixels[i] != 0)
            {
                count++
            }
        }
        return count
    }

    // puts an empty grid on the screen at the medium phone's card width
    @Before
    fun setup()
    {
        for (i in 0 until GRID_SQUARE_COUNT)
        {
            pixels.add(0)
        }
        composeTestRule.setContent {
            Box(Modifier.width(379.dp))
            {
                DrawGrid(pixels, teal, touchMode.intValue, ::paintSquare, ::startStroke, ::endStroke, Modifier)
            }
        }
    }

    // talkback can find the grid by its label
    @Test
    fun labelIsThere()
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).assertExists()
    }

    // one tap paints one square and counts as one stroke
    @Test
    fun tapPaintsOneSquare()
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(0, width))
            up()
        }
        assertEquals(teal, pixels[0])
        assertEquals(1, countPainted())
        assertEquals(1, strokeStarts)
        assertEquals(1, strokeEnds)
    }

    // one fast swipe across the top row paints all 8 squares
    @Test
    fun fastSwipeFillsTheRow()
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(0, width))
            moveTo(getSquareCenter(7, width))
            up()
        }
        for (i in 0 until GRID_SIZE)
        {
            assertEquals(teal, pixels[i])
        }
        assertEquals(8, countPainted())
    }

    // a second finger paints nothing and doesn't end the stroke
    @Test
    fun secondFingerPaintsNothing()
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(0, getSquareCenter(0, width))
            down(1, getSquareCenter(27, width))
            up(1)
            up(0)
        }
        assertEquals(1, countPainted())
        assertEquals(1, strokeStarts)
        assertEquals(1, strokeEnds)
    }

    // with pick on a finger down reports its one square, a drag and the rim add nothing, and off takes nothing
    @Test
    fun pickReportsOnceAndOffTakesNothing()
    {
        touchMode.intValue = GRID_TOUCH_PICK
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(9, width))
            moveTo(getSquareCenter(12, width))
            up()
            down(Offset(0f, 0f))
            up()
        }
        assertEquals(teal, pixels[9])
        assertEquals(1, paintCalls)
        touchMode.intValue = GRID_TOUCH_OFF
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(0, width))
            moveTo(getSquareCenter(7, width))
            up()
        }
        assertEquals(1, paintCalls)
        assertEquals(0, strokeStarts)
        assertEquals(0, strokeEnds)
    }
}
