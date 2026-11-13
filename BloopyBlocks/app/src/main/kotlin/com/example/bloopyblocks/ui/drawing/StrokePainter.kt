package com.example.bloopyblocks.ui.drawing

// activeId holds this when no finger is drawing
private const val NO_FINGER = -1L

// turns one finger's touch points into the squares to paint
class StrokePainter
{
    // the finger that owns the stroke
    private var activeId = NO_FINGER

    // where that finger was last
    private var lastX = 0f
    private var lastY = 0f

    // what this stroke painted so far, in case pixels hasn't caught up
    private val strokePainted = BooleanArray(GRID_SQUARE_COUNT)
    private val strokeColors = IntArray(GRID_SQUARE_COUNT)

    // when the last buzz went out
    private var lastTickMillis = 0L
    private var hasTicked = false

    // true while a finger is drawing
    fun isDrawing(): Boolean
    {
        return activeId != NO_FINGER
    }

    // starts a stroke and returns the squares to paint
    fun down(pointerId: Long, x: Float, y: Float, innerWidth: Float, pixels: List<Int>, color: Int): List<Int>
    {
        val toPaint = mutableListOf<Int>()
        // a second finger never joins in
        if (isDrawing())
        {
            return toPaint
        }
        activeId = pointerId
        lastX = x
        lastY = y
        addSquare(toPaint, findSquareAt(x, y, innerWidth), pixels, color)
        return toPaint
    }

    // returns every square to paint between the last point and this one
    fun move(pointerId: Long, x: Float, y: Float, innerWidth: Float, pixels: List<Int>, color: Int): List<Int>
    {
        val toPaint = mutableListOf<Int>()
        if (!isDrawing() || pointerId != activeId)
        {
            return toPaint
        }
        val crossed = findSquaresBetween(lastX, lastY, x, y, innerWidth)
        for (i in 0 until crossed.size)
        {
            addSquare(toPaint, crossed[i], pixels, color)
        }
        lastX = x
        lastY = y
        return toPaint
    }

    // ends the stroke when its own finger lifts, true if it ended
    fun up(pointerId: Long): Boolean
    {
        if (!isDrawing() || pointerId != activeId)
        {
            return false
        }
        endStroke()
        return true
    }

    // true when it's time for another buzz, and starts the 35 ms wait over
    fun canTick(nowMillis: Long): Boolean
    {
        if (hasTicked && nowMillis - lastTickMillis < GRID_TICK_GAP_MS)
        {
            return false
        }
        lastTickMillis = nowMillis
        hasTicked = true
        return true
    }

    // clears the stroke so the next finger starts fresh
    private fun endStroke()
    {
        activeId = NO_FINGER
        for (i in 0 until GRID_SQUARE_COUNT)
        {
            strokePainted[i] = false
        }
    }

    // adds one square to toPaint unless it's off the grid or already this color
    private fun addSquare(toPaint: MutableList<Int>, index: Int, pixels: List<Int>, color: Int)
    {
        if (index == -1)
        {
            return
        }
        // a square painted this stroke beats what pixels says
        var current = getPixelAt(pixels, index)
        if (strokePainted[index])
        {
            current = strokeColors[index]
        }
        if (current == color)
        {
            return
        }
        strokePainted[index] = true
        strokeColors[index] = color
        toPaint.add(index)
    }
}
