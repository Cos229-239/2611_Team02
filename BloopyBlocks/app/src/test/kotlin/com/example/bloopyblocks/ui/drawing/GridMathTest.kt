package com.example.bloopyblocks.ui.drawing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// square lookups, the fill-in between touch samples, and which squares join
class GridMathTest
{
    // inside width on the medium phone, the 379 card minus 1 dp padding each side
    private val inner = 377f

    // the two test colors
    private val teal = 0xFF4ECDC4.toInt()
    private val navy = 0xFF1E2A44.toInt()

    // builds a full grid from short rows, T is teal, N is navy, anything else is empty
    private fun grid(rows: List<String>): List<Int>
    {
        val pixels = MutableList(GRID_SQUARE_COUNT) { 0 }
        for (row in 0 until rows.size)
        {
            for (col in 0 until rows[row].length)
            {
                if (rows[row][col] == 'T')
                {
                    pixels[row * GRID_SIZE + col] = teal
                }
                else if (rows[row][col] == 'N')
                {
                    pixels[row * GRID_SIZE + col] = navy
                }
            }
        }
        return pixels
    }

    // all four corners land on the right squares
    @Test
    fun cornersLandOnTheRightSquares()
    {
        assertEquals(0, findSquareAt(1f, 1f, inner))
        assertEquals(7, findSquareAt(376f, 1f, inner))
        assertEquals(56, findSquareAt(1f, 376f, inner))
        assertEquals(63, findSquareAt(376f, 376f, inner))
    }

    // anything outside the grid is -1, all four edges
    @Test
    fun offTheGridIsMinusOne()
    {
        assertEquals(-1, findSquareAt(-1f, 10f, inner))
        assertEquals(-1, findSquareAt(10f, -1f, inner))
        assertEquals(-1, findSquareAt(377f, 10f, inner))
        assertEquals(-1, findSquareAt(10f, 377f, inner))
    }

    // square size at the design widths
    @Test
    fun squareSizeMatchesTheDesign()
    {
        assertEquals(39f, getGridSquareSize(328f), 0.001f)
        assertEquals(48f, getGridSquareSize(400f), 0.001f)
    }

    // a fast straight swipe hits every square on the way, across and down
    @Test
    fun fastSwipeFillsEverySquareOnTheWay()
    {
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), findSquaresBetween(5f, 5f, 372f, 5f, inner))
        assertEquals(listOf(8, 16, 24, 32, 40, 48, 56), findSquaresBetween(5f, 5f, 5f, 372f, inner))
    }

    // corner to corner hits only the diagonal
    @Test
    fun diagonalSwipeStaysOnTheDiagonal()
    {
        assertEquals(listOf(9, 18, 27, 36, 45, 54, 63), findSquaresBetween(5f, 5f, 372f, 372f, inner))
    }

    // the part of a swipe off the grid gets skipped
    @Test
    fun swipeOffTheGridIsSkipped()
    {
        assertEquals(listOf(0, 1), findSquaresBetween(-50f, 5f, 60f, 5f, inner))
    }

    // two of the same color side by side join, and the corners between them go square
    @Test
    fun sideBySideJoinAcrossTheGap()
    {
        val left = meltShape(grid(listOf("TT")), 0)
        assertTrue(left.joinRight)
        assertFalse(left.roundTopRight)
        assertFalse(left.roundBottomRight)
        assertTrue(left.roundTopLeft)
        assertTrue(left.roundBottomLeft)
    }

    // two of the same color stacked join down, and the bottom corners go square
    @Test
    fun stackedJoinDown()
    {
        val top = meltShape(grid(listOf("T", "T")), 0)
        assertTrue(top.joinDown)
        assertFalse(top.roundBottomLeft)
        assertFalse(top.roundBottomRight)
    }

    // different colors keep their gap
    @Test
    fun differentColorsKeepTheirGap()
    {
        val shape = meltShape(grid(listOf("TN")), 0)
        assertFalse(shape.joinRight)
        assertTrue(shape.roundTopRight)
    }

    // the little crossing fills when 3 or 4 squares around it share a color
    @Test
    fun crossingFillsAtThreeOrFour()
    {
        assertEquals(teal, crossingColor(grid(listOf("TT", "TT")), 0, 0))
        assertEquals(teal, crossingColor(grid(listOf("TT", "T.")), 0, 0))
        assertEquals(0, crossingColor(grid(listOf("T.", ".T")), 0, 0))
    }
}
