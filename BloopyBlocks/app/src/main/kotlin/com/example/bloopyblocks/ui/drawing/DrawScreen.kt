package com.example.bloopyblocks.ui.drawing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloopyblocks.R
import com.example.bloopyblocks.ui.theme.BloopBodyFont
import com.example.bloopyblocks.ui.theme.BloopCream
import com.example.bloopyblocks.ui.theme.BloopDark
import com.example.bloopyblocks.ui.theme.BloopIconGray
import com.example.bloopyblocks.ui.theme.BloopTeal
import kotlinx.coroutines.launch

// bloop's lines, then the words on the buttons
const val DRAW_SAY_OPEN = "Let's draw!"
const val DRAW_SAY_GOING = "Keep going!"
const val DRAW_SAY_FILL = "Tap a spot!"
const val DRAW_SAY_FILLED = "Ooh, nice!"
const val DRAW_WORD_UNDO = "Undo"
const val DRAW_WORD_FILL = "Fill"
const val DRAW_WORD_ERASE = "Erase"

// what talkback reads for the back arrow and for each pot, in tray order
const val DRAW_LABEL_BACK = "Back"
val DrawPotNames = listOf("Teal", "Yellow", "Coral", "Blue", "Brown", "Green", "Dark", DRAW_WORD_ERASE)

// the back arrow, pots in a tray row, a pressed piece's shrink, and the fill spread and a color swap in ms
private const val DRAW_BACK_ARROW = "←"
private const val DRAW_POTS_PER_ROW = 4
private const val DRAW_PRESS_SCALE = 0.97f
private const val DRAW_FILL_SPREAD_MS = 250
private const val DRAW_SWAP_MS = 150

// spacing from the design, the margin is also the gap above the tray and the gap is also the tray's padding
private val DrawMargin = 20.dp
private val DrawGap = 12.dp
private val DrawGridMargin = 16.dp
private val DrawPotRowGap = 8.dp
private val DrawTabRoom = 112.dp

// piece sizes, corners, the edge under a raised piece, the bubble's padding and the ring, all from the design
private val DrawTopRowHeight = 68.dp
private val DrawBackSize = 48.dp
private val DrawBloopSize = 60.dp
private val DrawUndoWidth = 84.dp
private val DrawFillWidth = 88.dp
private val DrawFillHeight = 60.dp
private val DrawPotSize = 56.dp
private val DrawPotRowHeight = 64.dp
private val DrawPieceRadius = 12.dp
private val DrawPieceEdge = 3.dp
private val DrawBubbleRadius = 14.dp
private val DrawBubblePadX = 14.dp
private val DrawBubblePadY = 10.dp
private val DrawRingWidth = 3.dp

// every word on the screen is nunito bold 16 in dark, the back arrow is bigger and lifted to sit centered
private val DrawTextStyle = TextStyle(color = BloopDark, fontSize = 16.sp, fontFamily = BloopBodyFont, letterSpacing = 0.sp, lineHeight = 20.sp)
private val DrawArrowStyle = DrawTextStyle.copy(fontSize = 22.sp, baselineShift = BaselineShift(0.27f))

// everything the draw screen remembers besides the drawing
class DrawScreenState
{
    // fill on, just filled, and a fill still spreading
    val isFillOn = mutableStateOf(false)
    val hasJustFilled = mutableStateOf(false)
    val isSpreading = mutableStateOf(false)

    // how far a fill has spread 0 to 1, the grid before this stroke or fill, the fill's squares, and if a stroke is open
    val spread = Animatable(0f)
    var before = DrawEmptyGrid
    var patch = emptyList<Int>()
    var isStroking = false
}

// what the navigation entry shows, the draw screen with a fresh state
@Composable
fun DrawRoute(onGoHome: () -> Unit)
{
    DrawScreen(remember { DrawScreenState() }, onGoHome)
}

// the screen itself, tests hand it their own state
@Composable
fun DrawScreen(state: DrawScreenState, onGoHome: () -> Unit)
{
    // the drawing, the picked pot and the undo steps live through a turn of the phone, each step is a whole grid
    val pixels = rememberSaveable { mutableStateOf(DrawEmptyGrid) }
    val pot = rememberSaveable { mutableIntStateOf(0) }
    val steps = rememberSaveable { mutableStateOf(emptyList<List<Int>>()) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // fills the patch around index, the squares land at once and the spread is only what the kid watches
    fun fill(index: Int, color: Int)
    {
        state.isFillOn.value = false
        state.patch = findFillSquares(pixels.value, index, color)
        // already that color, no step and no buzz
        if (state.patch.isEmpty())
        {
            return
        }
        state.before = pixels.value
        pixels.value = paintPatch(pixels.value, state.patch, state.patch.size, color)
        steps.value = addUndoStep(steps.value, state.before, pixels.value)
        state.hasJustFilled.value = true
        state.isSpreading.value = true
        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
        scope.launch {
            state.spread.animateTo(1f, tween(DRAW_FILL_SPREAD_MS, 0, LinearOutSlowInEasing))
            state.isSpreading.value = false
            state.spread.snapTo(0f)
        }
    }

    // the grid reports one square, painted in an open stroke, or filled from when Fill is on
    fun paint(index: Int)
    {
        val color = DrawPotColors[pot.intValue]
        if (state.isStroking)
        {
            pixels.value = paintSquare(pixels.value, index, color)
        }
        else if (state.isFillOn.value)
        {
            fill(index, color)
        }
    }

    // a finger went down to paint, keep the grid from before it
    fun startStroke()
    {
        state.isStroking = true
        state.before = pixels.value
        state.hasJustFilled.value = false
    }

    // the finger lifted, a stroke that changed something is one undo step
    fun endStroke()
    {
        state.isStroking = false
        steps.value = addUndoStep(steps.value, state.before, pixels.value)
    }

    // takes back the last stroke or fill, a stroke still going counts from here
    fun undo()
    {
        pixels.value = steps.value.last()
        steps.value = steps.value.dropLast(1)
        state.before = pixels.value
        state.hasJustFilled.value = false
    }

    // turns Fill on or off
    fun toggleFill()
    {
        state.isFillOn.value = !state.isFillOn.value
    }

    // while a fill spreads the grid shows the patch growing out from the tapped square, that square first
    val color = DrawPotColors[pot.intValue]
    var shown = pixels.value
    if (state.isSpreading.value)
    {
        shown = paintPatch(state.before, state.patch, 1 + (state.spread.value * (state.patch.size - 1)).toInt(), color)
    }
    // with Fill on the grid picks one square instead of painting
    var mode = GRID_TOUCH_PAINT
    if (state.isFillOn.value)
    {
        mode = GRID_TOUCH_PICK
    }
    // back always goes home, the phone's back too
    BackHandler(true, onGoHome)
    // the page only scrolls when it doesn't fit, so a press shows right away
    val scroll = rememberScrollState()
    BoxWithConstraints(Modifier.fillMaxSize().background(BloopCream).safeDrawingPadding())
    {
        // the page is at least the screen's height less the room kept for the tabs, so the spare space lands above Fill
        val pageHeight = maxHeight - DrawTabRoom
        Column(Modifier.fillMaxSize().verticalScroll(scroll, scroll.maxValue > 0))
        {
            Column(Modifier.heightIn(pageHeight))
            {
                DrawTopRow(getBloopLine(state, pixels.value), steps.value.size > 0, onGoHome, ::undo)
                DrawGrid(shown, color, mode, ::paint, ::startStroke, ::endStroke, Modifier.align(Alignment.CenterHorizontally).padding(DrawGridMargin, 0.dp))
                DrawTray(pot, Modifier.padding(DrawMargin, DrawMargin, DrawMargin, DrawGap))
                Spacer(Modifier.weight(1f))
                // the Fill button keeps room under it for its edge, and tells talkback when it's on
                val fillLook = Modifier
                    .padding(DrawMargin, 0.dp, 0.dp, GridEdgeHeight)
                    .size(DrawFillWidth, DrawFillHeight)
                    .semantics { selected = state.isFillOn.value }
                DrawButton(DRAW_WORD_FILL, DrawTextStyle, state.isFillOn.value, true, fillLook, ::toggleFill)
            }
            // the room for the tabs gives before anything scrolls
            Spacer(Modifier.weight(1f, false).height(DrawTabRoom))
        }
        // nothing takes a tap while a fill spreads
        if (state.isSpreading.value)
        {
            val catcher = Modifier.matchParentSize().pointerInput(Unit)
            {
            }
            Box(catcher)
        }
    }
}

// returns bloop's line for this moment
private fun getBloopLine(state: DrawScreenState, pixels: List<Int>): String
{
    if (state.isFillOn.value)
    {
        return DRAW_SAY_FILL
    }
    if (state.hasJustFilled.value)
    {
        return DRAW_SAY_FILLED
    }
    if (hasDrawing(pixels))
    {
        return DRAW_SAY_GOING
    }
    return DRAW_SAY_OPEN
}

// returns pixels with the first count squares of patch painted
private fun paintPatch(pixels: List<Int>, patch: List<Int>, count: Int, color: Int): List<Int>
{
    var painted = pixels
    for (i in 0 until count)
    {
        painted = paintSquare(painted, patch[i], color)
    }
    return painted
}

// the row above the grid, back, bloop with his bubble, and undo pushed to the right
@Composable
private fun DrawTopRow(line: String, canUndo: Boolean, onBack: () -> Unit, onUndo: () -> Unit)
{
    Row(Modifier.fillMaxWidth().heightIn(DrawTopRowHeight).padding(DrawMargin, 0.dp), Arrangement.spacedBy(DrawGap), Alignment.CenterVertically)
    {
        // the bubble hugs its words and takes the room that's left
        val bubble = Modifier
            .weight(1f)
            .wrapContentWidth(Alignment.Start)
            .drawBehind { drawRaised(this, GridCardWhite, GridCardEdge, DrawBubbleRadius, DrawPieceEdge) }
            .padding(DrawBubblePadX, DrawBubblePadY)
        DrawButton(DRAW_BACK_ARROW, DrawArrowStyle, false, true, Modifier.size(DrawBackSize).semantics { contentDescription = DRAW_LABEL_BACK }, onBack)
        Image(painterResource(R.drawable.bloop_clapping), null, Modifier.size(DrawBloopSize))
        Text(line, bubble, style = DrawTextStyle)
        DrawButton(DRAW_WORD_UNDO, DrawTextStyle, false, canUndo, Modifier.size(DrawUndoWidth, DrawBackSize), onUndo)
    }
}

// the white tray under the grid, 8 pots in 2 rows of 4
@Composable
private fun DrawTray(pot: MutableIntState, modifier: Modifier)
{
    val card = modifier
        .drawBehind { drawRaised(this, GridCardWhite, GridCardEdge, GridCardRadius, GridEdgeHeight) }
        .padding(DrawGap)
    Column(card, Arrangement.spacedBy(DrawPotRowGap))
    {
        for (row in 0 until DrawPotColors.size / DRAW_POTS_PER_ROW)
        {
            Row(Modifier.fillMaxWidth().height(DrawPotRowHeight), Arrangement.SpaceAround, Alignment.CenterVertically)
            {
                for (col in 0 until DRAW_POTS_PER_ROW)
                {
                    DrawPot(row * DRAW_POTS_PER_ROW + col, pot)
                }
            }
        }
    }
}

// one paint pot, a tap picks it and the picked one wears the ring
@Composable
private fun DrawPot(index: Int, pot: MutableIntState)
{
    val source = remember { MutableInteractionSource() }
    val isPicked = pot.intValue == index
    var scale = 1f
    if (source.collectIsPressedAsState().value)
    {
        scale = DRAW_PRESS_SCALE
    }

    // picks this pot
    fun pick()
    {
        pot.intValue = index
    }

    val look = Modifier
        .size(DrawPotSize)
        .scale(scale)
        .drawBehind { drawPot(this, getSquareColor(DrawPotColors[index]), isPicked) }
        .selectable(isPicked, source, null, true, Role.RadioButton, ::pick)
        .semantics { contentDescription = DrawPotNames[index] }
    Box(look, Alignment.Center)
    {
        if (index == DRAW_ERASE_POT)
        {
            Text(DRAW_WORD_ERASE, style = DrawTextStyle)
        }
    }
}

// one raised button, teal on a dark edge when picked, gray and sunk flat when it can't be tapped
@Composable
private fun DrawButton(text: String, style: TextStyle, isPicked: Boolean, isEnabled: Boolean, modifier: Modifier, onClick: () -> Unit)
{
    val source = remember { MutableInteractionSource() }
    var fill = GridCardWhite
    var edge = GridCardEdge
    var lift = DrawPieceEdge
    var scale = 1f
    if (isPicked)
    {
        fill = BloopTeal
        edge = BloopDark
    }
    if (!isEnabled)
    {
        fill = BloopIconGray
        lift = 0.dp
    }
    // a press drops it onto its edge and shrinks it a touch
    if (source.collectIsPressedAsState().value)
    {
        lift = 0.dp
        scale = DRAW_PRESS_SCALE
    }
    // a change of look fades from the old colors to the new
    val shownFill = animateColorAsState(fill, tween(DRAW_SWAP_MS))
    val shownEdge = animateColorAsState(edge, tween(DRAW_SWAP_MS))
    val look = modifier
        .scale(scale)
        .offset(0.dp, DrawPieceEdge - lift)
        .drawBehind { drawRaised(this, shownFill.value, shownEdge.value, DrawPieceRadius, lift) }
        .clickable(source, null, isEnabled, null, Role.Button, onClick)
    Box(look, Alignment.Center)
    {
        Text(text, style = style)
    }
}

// draws a raised piece, the edge is the same shape moved down and the fill covers the rest of it
private fun drawRaised(scope: DrawScope, fill: Color, edge: Color, radius: Dp, edgeHeight: Dp)
{
    val corner = CornerRadius(toPx(scope, radius), toPx(scope, radius))
    scope.drawRoundRect(edge, Offset(0f, toPx(scope, edgeHeight)), scope.size, corner)
    scope.drawRoundRect(fill, Offset(0f, 0f), scope.size, corner)
}

// draws a pot's circle, the picked one gets a dark ring with a white gap around it
private fun drawPot(scope: DrawScope, color: Color, isPicked: Boolean)
{
    val radius = scope.size.width / 2
    val ring = toPx(scope, DrawRingWidth)
    if (isPicked)
    {
        scope.drawCircle(BloopDark, radius + ring * 2)
        scope.drawCircle(GridCardWhite, radius + ring)
    }
    scope.drawCircle(color, radius)
}
