package com.example.bloopyblocks.ui.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot

// the grid is 8 by 8
const val GRID_SIZE = 8
const val GRID_SQUARE_COUNT = GRID_SIZE * GRID_SIZE

// gap between squares and padding inside the card, in dp
const val GRID_GAP_DP = 2f
const val GRID_PAD_DP = 1f

// one tick at most every 35 ms so a fast swipe doesn't buzz nonstop
const val GRID_TICK_GAP_MS = 35L

// what talkback reads out for the grid
const val GRID_LABEL = "Drawing grid, 8 by 8"

// fill-in checks per square width along a swipe
private const val SAMPLES_PER_SQUARE = 3

// a crossing fills when this many of its 4 squares share a color
private const val CROSSING_MIN_SAME = 3

// card and square sizes from the design
val GridGap = GRID_GAP_DP.dp
val GridPadding = GRID_PAD_DP.dp
val GridCardRadius = 16.dp
val GridSquareRadius = 4.dp
val GridOuterCornerRadius = 15.dp
val GridEdgeHeight = 4.dp
val GridMaxWidth = 400.dp
val GridBackSwipeBlock = 200.dp

// grid colors from the design system, kept here until the team has a bloopy theme
val GridCardWhite = Color(0xFFFFFFFF)
val GridCardEdge = Color(0xFFE7E0D4)
val GridEmptySquare = Color(0xFFE7E0D4)

// how one square joins its neighbors and which corners stay round
class MeltShape
{
    var joinRight = false
    var joinDown = false
    var roundTopLeft = false
    var roundTopRight = false
    var roundBottomRight = false
    var roundBottomLeft = false
}

// returns the square under a point, row by row, or -1 off the grid
// x, y and innerWidth are px, measured inside the card's padding
fun findSquareAt(x: Float, y: Float, innerWidth: Float): Int
{
    // off any of the four edges
    if (x < 0f || y < 0f || x >= innerWidth || y >= innerWidth)
    {
        return -1
    }
    val step = innerWidth / GRID_SIZE
    // rounding can land on 8 right at the edge, keep it on the last square
    val col = minOf(floor(x / step).toInt(), GRID_SIZE - 1)
    val row = minOf(floor(y / step).toInt(), GRID_SIZE - 1)
    return row * GRID_SIZE + col
}

// returns the squares a straight swipe passes through, in order, start square left out
// the points and innerWidth are px, measured inside the card's padding
fun findSquaresBetween(fromX: Float, fromY: Float, toX: Float, toY: Float, innerWidth: Float): List<Int>
{
    val crossed = mutableListOf<Int>()
    // no grid to walk until it has a width
    if (innerWidth <= 0f)
    {
        return crossed
    }
    // how many checks the swipe needs, at least one
    val sampleGap = innerWidth / GRID_SIZE / SAMPLES_PER_SQUARE
    val length = hypot(toX - fromX, toY - fromY)
    val samples = maxOf(1, ceil(length / sampleGap).toInt())
    var last = findSquareAt(fromX, fromY, innerWidth)
    for (i in 1..samples)
    {
        // the point this far along the swipe
        val t = i.toFloat() / samples
        var x = fromX + (toX - fromX) * t
        var y = fromY + (toY - fromY) * t
        // the last check is the real end point, the math can land a hair off it
        if (i == samples)
        {
            x = toX
            y = toY
        }
        val square = findSquareAt(x, y, innerWidth)
        // only a new square on the grid counts
        if (square != -1 && square != last)
        {
            crossed.add(square)
            last = square
        }
    }
    return crossed
}

// returns one square's width in dp, cardWidth is the whole card in dp
fun getGridSquareSize(cardWidth: Float): Float
{
    return (cardWidth - GRID_PAD_DP * 2 - GRID_GAP_DP * (GRID_SIZE - 1)) / GRID_SIZE
}

// returns the color at index, or 0 past the end of the list
fun pixelAt(pixels: List<Int>, index: Int): Int
{
    if (index < 0 || index >= pixels.size)
    {
        return 0
    }
    return pixels[index]
}

// returns the color at a row and col, or 0 off the grid
private fun colorAt(pixels: List<Int>, row: Int, col: Int): Int
{
    if (row < 0 || col < 0 || row >= GRID_SIZE || col >= GRID_SIZE)
    {
        return 0
    }
    return pixelAt(pixels, row * GRID_SIZE + col)
}

// true when the square at row, col holds this color, empty never counts
private fun isSameColor(pixels: List<Int>, row: Int, col: Int, color: Int): Boolean
{
    if (color == 0)
    {
        return false
    }
    return colorAt(pixels, row, col) == color
}

// returns the joins and corners for the square at index
fun meltShape(pixels: List<Int>, index: Int): MeltShape
{
    val row = index / GRID_SIZE
    val col = index % GRID_SIZE
    val color = pixelAt(pixels, index)
    // which of the four sides touch the same color
    val up = isSameColor(pixels, row - 1, col, color)
    val down = isSameColor(pixels, row + 1, col, color)
    val left = isSameColor(pixels, row, col - 1, color)
    val right = isSameColor(pixels, row, col + 1, color)
    val shape = MeltShape()
    shape.joinRight = right
    shape.joinDown = down
    // a corner stays round unless one of its two sides joins
    shape.roundTopLeft = !up && !left
    shape.roundTopRight = !up && !right
    shape.roundBottomRight = !down && !right
    shape.roundBottomLeft = !down && !left
    return shape
}

// counts how many colors in the list match this one
private fun countSame(colors: List<Int>, color: Int): Int
{
    var count = 0
    for (i in 0 until colors.size)
    {
        if (colors[i] == color)
        {
            count++
        }
    }
    return count
}

// returns the color for the crossing below and right of square row, col, or 0
fun crossingColor(pixels: List<Int>, row: Int, col: Int): Int
{
    // the four squares around the crossing
    val around = listOf(
        colorAt(pixels, row, col),
        colorAt(pixels, row, col + 1),
        colorAt(pixels, row + 1, col),
        colorAt(pixels, row + 1, col + 1)
    )
    for (i in 0 until around.size)
    {
        // empty never fills a crossing
        if (around[i] != 0 && countSame(around, around[i]) >= CROSSING_MIN_SAME)
        {
            return around[i]
        }
    }
    return 0
}
