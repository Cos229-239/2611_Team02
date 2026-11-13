package com.example.bloopyblocks.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// the four squares that sit in the card's own corners
private const val TOP_LEFT_SQUARE = 0
private const val TOP_RIGHT_SQUARE = GRID_SIZE - 1
private const val BOTTOM_LEFT_SQUARE = GRID_SQUARE_COUNT - GRID_SIZE
private const val BOTTOM_RIGHT_SQUARE = GRID_SQUARE_COUNT - 1

// joined pieces overlap by this many px so no seam shows
private const val SEAM_PX = 1f

// the 8 by 8 grid a kid paints on, pixels holds 64 colors row by row and 0 is empty
@Composable
fun DrawGrid(
    pixels: List<Int>,
    paintColor: Int,
    onPaint: (Int) -> Unit,
    onStrokeStart: () -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier
)
{
    val painter = remember { StrokePainter() }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    // the touch code keeps running between redraws, these always hold the newest values
    val pixelsNow = rememberUpdatedState(pixels)
    val colorNow = rememberUpdatedState(paintColor)
    val onPaintNow = rememberUpdatedState(onPaint)
    val onStartNow = rememberUpdatedState(onStrokeStart)
    val onEndNow = rememberUpdatedState(onStrokeEnd)
    // fills the width up to the max and stays square
    val gridModifier = modifier
        .widthIn(0.dp, GridMaxWidth)
        .fillMaxWidth()
        .aspectRatio(1f)
        .semantics { contentDescription = GRID_LABEL }
        .systemGestureExclusion { getBackSwipeRect(it.size.width.toFloat(), it.size.height.toFloat(), density) }
        .pointerInput(Unit)
        {
            awaitPointerEventScope {
                while (true)
                {
                    val event = awaitPointerEvent()
                    val pad = GridPadding.value * density
                    val innerWidth = size.width - pad * 2
                    for (i in 0 until event.changes.size)
                    {
                        val change = event.changes[i]
                        val squares = handleFinger(painter, change, pad, innerWidth, pixelsNow.value, colorNow.value, onStartNow.value, onEndNow.value)
                        paintSquares(squares, painter, change.uptimeMillis, haptic, onPaintNow.value)
                        change.consume()
                    }
                }
            }
        }
    Canvas(gridModifier)
    {
        drawCard(this)
        drawSquares(this, pixels)
        drawCrossings(this, pixels)
    }
}

// runs one finger change through the painter, returns the squares to paint
private fun handleFinger(
    painter: StrokePainter,
    change: PointerInputChange,
    pad: Float,
    innerWidth: Float,
    pixels: List<Int>,
    color: Int,
    onStrokeStart: () -> Unit,
    onStrokeEnd: () -> Unit
): List<Int>
{
    // the painter works in px inside the card's padding
    val id = change.id.value
    val x = change.position.x - pad
    val y = change.position.y - pad
    if (change.pressed && !change.previousPressed)
    {
        val wasDrawing = painter.isDrawing()
        val squares = painter.down(id, x, y, innerWidth, pixels, color)
        if (!wasDrawing && painter.isDrawing())
        {
            onStrokeStart()
        }
        return squares
    }
    // a lifted finger and a stroke the system took away both end here
    if (!change.pressed && change.previousPressed)
    {
        if (painter.up(id))
        {
            onStrokeEnd()
        }
        return emptyList()
    }
    if (change.pressed)
    {
        return painter.move(id, x, y, innerWidth, pixels, color)
    }
    return emptyList()
}

// sends each square to onPaint, then one light buzz if the painter says it's time
private fun paintSquares(squares: List<Int>, painter: StrokePainter, time: Long, haptic: HapticFeedback, onPaint: (Int) -> Unit)
{
    for (i in 0 until squares.size)
    {
        onPaint(squares[i])
    }
    if (squares.size > 0 && painter.canTick(time))
    {
        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
    }
}

// returns the strip where android's back swipe is turned off, centered on the grid
private fun getBackSwipeRect(width: Float, height: Float, density: Float): Rect
{
    val blockHeight = GridBackSwipeBlock.value * density
    var top = (height - blockHeight) / 2
    if (top < 0f)
    {
        top = 0f
    }
    return Rect(0f, top, width, top + blockHeight)
}

// turns dp into px for this screen
private fun toPx(scope: DrawScope, dp: Dp): Float
{
    return dp.value * scope.density
}

// returns one square's width in px
private fun getSquarePx(scope: DrawScope): Float
{
    return getGridSquareSize(scope.size.width / scope.density) * scope.density
}

// returns the fill for a square, linen when it's empty
private fun getSquareColor(color: Int): Color
{
    if (color == 0)
    {
        return GridEmptySquare
    }
    return Color(color)
}

// returns one corner's radius, square when it joins and bigger on the card's own corner
private fun getCorner(scope: DrawScope, isRound: Boolean, isCardCorner: Boolean): CornerRadius
{
    var radius = 0f
    if (isRound)
    {
        radius = toPx(scope, GridSquareRadius)
    }
    if (isRound && isCardCorner)
    {
        radius = toPx(scope, GridOuterCornerRadius)
    }
    return CornerRadius(radius, radius)
}

// draws the white card with its linen edge under it
private fun drawCard(scope: DrawScope)
{
    val radius = CornerRadius(toPx(scope, GridCardRadius), toPx(scope, GridCardRadius))
    // the edge is the same shape moved down, the card covers the rest of it
    scope.drawRoundRect(GridCardEdge, Offset(0f, toPx(scope, GridEdgeHeight)), scope.size, radius)
    scope.drawRoundRect(GridCardWhite, Offset(0f, 0f), scope.size, radius)
}

// draws all 64 squares, a painted one fills the gap to a same color neighbor
private fun drawSquares(scope: DrawScope, pixels: List<Int>)
{
    val pad = toPx(scope, GridPadding)
    val gap = toPx(scope, GridGap)
    val square = getSquarePx(scope)
    for (index in 0 until GRID_SQUARE_COUNT)
    {
        val shape = getMeltShape(pixels, index)
        // where the square sits
        val left = pad + (index % GRID_SIZE) * (square + gap)
        val top = pad + (index / GRID_SIZE) * (square + gap)
        val right = left + square
        val bottom = top + square
        val color = getSquareColor(getPixelAt(pixels, index))
        // a side that joins gets a strip over the gap, a little more each way so no seam shows
        if (shape.joinRight)
        {
            scope.drawRect(color, Offset(right - SEAM_PX, top), Size(gap + SEAM_PX * 2, square))
        }
        if (shape.joinDown)
        {
            scope.drawRect(color, Offset(left, bottom - SEAM_PX), Size(square, gap + SEAM_PX * 2))
        }
        val topLeft = getCorner(scope, shape.roundTopLeft, index == TOP_LEFT_SQUARE)
        val topRight = getCorner(scope, shape.roundTopRight, index == TOP_RIGHT_SQUARE)
        val bottomRight = getCorner(scope, shape.roundBottomRight, index == BOTTOM_RIGHT_SQUARE)
        val bottomLeft = getCorner(scope, shape.roundBottomLeft, index == BOTTOM_LEFT_SQUARE)
        val path = Path()
        path.addRoundRect(RoundRect(left, top, right, bottom, topLeft, topRight, bottomRight, bottomLeft))
        scope.drawPath(path, color)
    }
}

// fills the little crossing between 4 squares when all 4 share a color
private fun drawCrossings(scope: DrawScope, pixels: List<Int>)
{
    val pad = toPx(scope, GridPadding)
    val gap = toPx(scope, GridGap)
    val square = getSquarePx(scope)
    for (row in 0 until GRID_SIZE - 1)
    {
        for (col in 0 until GRID_SIZE - 1)
        {
            val color = getCrossingColor(pixels, row, col)
            if (color != 0)
            {
                // the crossing sits just past the square's bottom right corner, a little more each way
                val left = pad + col * (square + gap) + square - SEAM_PX
                val top = pad + row * (square + gap) + square - SEAM_PX
                scope.drawRect(Color(color), Offset(left, top), Size(gap + SEAM_PX * 2, gap + SEAM_PX * 2))
            }
        }
    }
}
