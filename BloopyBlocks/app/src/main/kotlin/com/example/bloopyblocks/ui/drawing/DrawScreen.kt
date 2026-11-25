package com.example.bloopyblocks.ui.drawing

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.bloopyblocks.BloopyBlocksApplication
import com.example.bloopyblocks.R
import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import com.example.bloopyblocks.ui.theme.BloopBodyFont
import com.example.bloopyblocks.ui.theme.BloopCoral
import com.example.bloopyblocks.ui.theme.BloopCream
import com.example.bloopyblocks.ui.theme.BloopCreamDeep
import com.example.bloopyblocks.ui.theme.BloopDark
import com.example.bloopyblocks.ui.theme.BloopIconGray
import com.example.bloopyblocks.ui.theme.BloopTeal
import com.example.bloopyblocks.ui.theme.BloopTitleFont
import java.util.UUID
import kotlinx.coroutines.launch

// bloop's lines, then the words on the buttons
const val DRAW_SAY_OPEN = "Let's draw!"
const val DRAW_SAY_GOING = "Keep going!"
const val DRAW_SAY_FILL = "Tap a spot!"
const val DRAW_SAY_FILLED = "Ooh, nice!"
const val DRAW_WORD_UNDO = "Undo"
const val DRAW_WORD_FILL = "Fill"
const val DRAW_WORD_ERASE = "Erase"

// the words on the two popups and on the button that opens the save one
const val DRAW_WORD_SAVE_BLOCK = "Save Block"
const val DRAW_SAY_NAME = "Name your block"
const val DRAW_SAY_NO_NAME = "Bloops, what should we call it?"
const val DRAW_SAY_FAILED = "Bloops, that didn't save. Try again!"
const val DRAW_WORD_NOT_YET = "Not yet"
const val DRAW_WORD_SAVE = "Save"
const val DRAW_SAY_UNSAVED = "Wait, it's not saved!"
const val DRAW_SAY_LOSE = "If you leave, this block goes away"
const val DRAW_WORD_SAVE_MINE = "Save my block"
const val DRAW_WORD_KEEP = "Keep drawing"
const val DRAW_WORD_LEAVE = "Leave"

// what talkback reads for the back arrow and for each pot, in tray order
const val DRAW_LABEL_BACK = "Back"
val DrawPotNames = listOf("Teal", "Yellow", "Coral", "Blue", "Brown", "Green", "Dark", DRAW_WORD_ERASE)

// the back arrow, pots in a tray row, a pressed piece's shrink, and the fill spread and a color swap in ms
private const val DRAW_BACK_ARROW = "←"
private const val DRAW_POTS_PER_ROW = 4
private const val DRAW_PRESS_SCALE = 0.97f
private const val DRAW_FILL_SPREAD_MS = 250
private const val DRAW_SWAP_MS = 150

// which popup is up, how a button looks, and the numbers behind the popups' moves
private const val DRAW_POPUP_NONE = 0
private const val DRAW_POPUP_LEAVE = 1
private const val DRAW_POPUP_SAVE = 2
private const val DRAW_LOOK_WHITE = 0
private const val DRAW_LOOK_TEAL = 1
private const val DRAW_LOOK_OUTLINE = 2
private const val DRAW_LOOK_QUIET = 3
private const val DRAW_POPUP_IN_MS = 200
private const val DRAW_POPUP_OUT_MS = 160
private const val DRAW_SHAKE_STEP_MS = 60
private const val DRAW_PULSE_HALF_MS = 100
private const val DRAW_PULSE_SCALE = 1.03f
private val DrawShakeSwings = listOf(8f, -6f, 4f, -2f, 0f)

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

// popup sizes from the design, the shell's corner and padding are both 24, then the picture, line, button and bloop
private val DrawPopupWidth = 351.dp
private val DrawPopupSide = 30.dp
private val DrawPopupPad = 24.dp
private val DrawPopupGap = 16.dp
private val DrawPictureSize = 112.dp
private val DrawPictureRadius = 6.dp
private val DrawLineHeight = 22.dp
private val DrawPopButtonHeight = 52.dp
private val DrawSitSize = 88.dp
private val DrawSitDrop = 16.dp

// the darker teal under a main button, the team's colors don't have it yet
private val DrawTealEdge = Color(0xFF2A9D8F)

// the body words are nunito bold 16 in dark, the back arrow is bigger and lifted to sit centered
private val DrawTextStyle = TextStyle(color = BloopDark, fontSize = 16.sp, fontFamily = BloopBodyFont, letterSpacing = 0.sp, lineHeight = 20.sp)
private val DrawArrowStyle = DrawTextStyle.copy(fontSize = 22.sp, baselineShift = BaselineShift(0.27f))

// titles and the teal and outline buttons are fredoka, and a popup's line sits centered
private val DrawTitleStyle = DrawTextStyle.copy(fontSize = 22.sp, fontFamily = BloopTitleFont, lineHeight = 26.sp, textAlign = TextAlign.Center)
private val DrawSaveStyle = DrawTextStyle.copy(fontSize = 20.sp, fontFamily = BloopTitleFont)
private val DrawPopStyle = DrawTextStyle.copy(fontFamily = BloopTitleFont)
private val DrawLineStyle = DrawTextStyle.copy(lineHeight = 22.sp, textAlign = TextAlign.Center)

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

    // which popup is up, taps behind it, the typed name, the line under the name field, and a save still running
    val popup = mutableIntStateOf(DRAW_POPUP_NONE)
    val pulses = mutableIntStateOf(0)
    val name = mutableStateOf("")
    val line = mutableStateOf("")
    val isSaving = mutableStateOf(false)

    // how far each popup and the line have come in 0 to 1, the popup's pulse, and the field's shake in dp
    val leaveShow = Animatable(0f)
    val saveShow = Animatable(0f)
    val lineShow = Animatable(0f)
    val pulse = Animatable(1f)
    val shake = Animatable(0f)

    // where back, Save Block and Save my block sit, where the save popup grows from, and if Remove animations was on
    var backCenter = Offset.Zero
    var saveCenter = Offset.Zero
    var saveMineCenter = Offset.Zero
    var saveOrigin = Offset.Zero
    var isMotionOff = false
}

// a clock that keeps a fade at full speed when the phone's Remove animations setting is on
private class DrawFullSpeed : MotionDurationScale
{
    override val scaleFactor = 1f
}

// what the navigation entry shows, the draw screen with a fresh state and the app's block store
@Composable
fun DrawRoute(onGoHome: () -> Unit)
{
    val context = LocalContext.current.applicationContext
    // a list so nothing is ever null, empty until the store is made and still empty if that fails
    val repositories = remember { mutableStateOf(emptyList<BlockRepository>()) }
    LaunchedEffect(Unit)
    {
        try
        {
            repositories.value = listOf((context as BloopyBlocksApplication).appContainer.getBlockRepository())
        }
        catch (e: Exception)
        {
            // no sign in or no database, Draw still opens and paints and Save shows its fail line
        }
    }
    DrawScreen(repositories.value, remember { DrawScreenState() }, onGoHome)
}

// the screen itself, tests hand it their own block store and state
@Composable
fun DrawScreen(repositories: List<BlockRepository>, state: DrawScreenState, onGoHome: () -> Unit)
{
    // the drawing, the picked pot and the undo steps live through a turn of the phone, each step is a whole grid
    val pixels = rememberSaveable { mutableStateOf(DrawEmptyGrid) }
    val pot = rememberSaveable { mutableIntStateOf(0) }
    val steps = rememberSaveable { mutableStateOf(emptyList<List<Int>>()) }
    // the block's id is made once, so a second try at saving uses the same one
    val blockId = rememberSaveable { UUID.randomUUID().toString() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
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

    // brings a show amount in or out at full speed, so it still fades with Remove animations on
    fun show(amount: Animatable<Float, AnimationVector1D>, isIn: Boolean)
    {
        var target = 0f
        var time = DRAW_POPUP_OUT_MS
        if (isIn)
        {
            target = 1f
            time = DRAW_POPUP_IN_MS
        }
        scope.launch(DrawFullSpeed())
        {
            amount.animateTo(target, tween(time, 0, LinearOutSlowInEasing))
        }
    }

    // puts one popup up or none, its show amount comes in and the other one's and the name line's go out
    fun setPopup(which: Int)
    {
        // the save popup grows out of Save my block when the leave popup opened it, any other time it's Save Block
        state.saveOrigin = state.saveCenter
        if (state.popup.intValue == DRAW_POPUP_LEAVE)
        {
            state.saveOrigin = state.saveMineCenter
        }
        // the phone's Remove animations setting, read as the popup opens
        state.isMotionOff = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        state.popup.intValue = which
        show(state.leaveShow, which == DRAW_POPUP_LEAVE)
        show(state.saveShow, which == DRAW_POPUP_SAVE)
        show(state.lineShow, false)
    }

    // the Save Block and Save my block buttons, the save popup opens with an empty name
    fun askName()
    {
        state.name.value = ""
        setPopup(DRAW_POPUP_SAVE)
    }

    // the Keep drawing and Not yet buttons, the popup leaves
    fun closePopup()
    {
        setPopup(DRAW_POPUP_NONE)
    }

    // the Leave button, only while its popup is the one up, a tap as it shrinks away loses nothing
    fun leave()
    {
        if (state.popup.intValue == DRAW_POPUP_LEAVE)
        {
            onGoHome()
        }
    }

    // back from the button or the phone, closes a popup, asks first when there's a drawing, or goes home
    fun goBack()
    {
        if (state.isSaving.value)
        {
            return
        }
        if (state.popup.intValue != DRAW_POPUP_NONE)
        {
            closePopup()
        }
        else if (hasDrawing(pixels.value))
        {
            setPopup(DRAW_POPUP_LEAVE)
        }
        else
        {
            onGoHome()
        }
    }

    // a tap behind a popup gives it one small pulse, with Remove animations on it stays still
    fun nudge()
    {
        state.pulses.intValue++
        scope.launch {
            state.pulse.animateTo(DRAW_PULSE_SCALE, tween(DRAW_PULSE_HALF_MS))
            state.pulse.animateTo(1f, tween(DRAW_PULSE_HALF_MS))
        }
    }

    // typing a name, it stops at 16 letters and the line under the field leaves
    fun type(typed: String)
    {
        state.name.value = limitBlockName(typed)
        show(state.lineShow, false)
    }

    // a Save with no name, the line asks kindly and the field turns coral and shakes, nothing buzzes
    fun remind()
    {
        state.line.value = DRAW_SAY_NO_NAME
        show(state.lineShow, true)
        if (state.isMotionOff)
        {
            return
        }
        scope.launch {
            state.shake.snapTo(0f)
            for (i in 0 until DrawShakeSwings.size)
            {
                state.shake.animateTo(DrawShakeSwings[i], tween(DRAW_SHAKE_STEP_MS, 0, LinearOutSlowInEasing))
            }
        }
    }

    // the popup's Save, a name saves the block once and the kid lands home, a save that fails says so
    fun save()
    {
        if (state.isSaving.value || state.popup.intValue != DRAW_POPUP_SAVE)
        {
            return
        }
        val name = cleanBlockName(state.name.value)
        if (name.isEmpty())
        {
            remind()
            return
        }
        state.isSaving.value = true
        show(state.lineShow, false)
        scope.launch {
            if (saveBlock(repositories, BlockEntity(blockId, name, pixels.value)))
            {
                onGoHome()
            }
            else
            {
                state.isSaving.value = false
                state.line.value = DRAW_SAY_FAILED
                show(state.lineShow, true)
            }
        }
    }

    // while a fill spreads the grid shows the patch growing out from the tapped square, that square first
    val color = DrawPotColors[pot.intValue]
    var shown = pixels.value
    if (state.isSpreading.value)
    {
        shown = paintPatch(state.before, state.patch, 1 + (state.spread.value * (state.patch.size - 1)).toInt(), color)
    }
    // with Fill on the grid picks one square instead of painting, behind a popup it takes nothing
    val isFree = state.popup.intValue == DRAW_POPUP_NONE
    var mode = GRID_TOUCH_PAINT
    if (state.isFillOn.value)
    {
        mode = GRID_TOUCH_PICK
    }
    if (!isFree)
    {
        mode = GRID_TOUCH_OFF
    }
    // back asks first when there's a drawing, the phone's back too
    BackHandler(true, ::goBack)
    // the page only scrolls when it doesn't fit, so a press shows right away
    val scroll = rememberScrollState()
    // the popups sit outside the phone's bars padding, so the spot they grow from lines up with the buttons
    Box(Modifier.fillMaxSize().background(BloopCream))
    {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding())
        {
            // the page is at least the screen's height less the room kept for the tabs, so the spare space lands above Fill
            val pageHeight = maxHeight - DrawTabRoom
            Column(Modifier.fillMaxSize().verticalScroll(scroll, scroll.maxValue > 0))
            {
                Column(Modifier.heightIn(pageHeight))
                {
                    DrawTopRow(state, getBloopLine(state, pixels.value), steps.value.size > 0 && isFree, isFree, ::goBack, ::undo)
                    DrawGrid(shown, color, mode, ::paint, ::startStroke, ::endStroke, Modifier.align(Alignment.CenterHorizontally).padding(DrawGridMargin, 0.dp))
                    DrawTray(pot, isFree, Modifier.padding(DrawMargin, DrawMargin, DrawMargin, DrawGap))
                    Spacer(Modifier.weight(1f))
                    // the row keeps room under it for the buttons' edges
                    Row(Modifier.padding(DrawMargin, 0.dp, DrawMargin, GridEdgeHeight), Arrangement.spacedBy(DrawGap))
                    {
                        // the Fill button tells talkback when it's on, Save Block notes where it sits for its popup
                        val fillLook = Modifier
                            .size(DrawFillWidth, DrawFillHeight)
                            .semantics { selected = state.isFillOn.value }
                        val saveLook = Modifier
                            .weight(1f)
                            .height(DrawFillHeight)
                            .onGloballyPositioned { state.saveCenter = it.localToRoot(Offset(it.size.width / 2f, it.size.height / 2f)) }
                        DrawButton(DRAW_WORD_FILL, DrawTextStyle, DRAW_LOOK_WHITE, state.isFillOn.value, isFree, fillLook, ::toggleFill)
                        DrawButton(DRAW_WORD_SAVE_BLOCK, DrawSaveStyle, DRAW_LOOK_TEAL, false, isFree && hasDrawing(pixels.value), saveLook, ::askName)
                    }
                }
                // the room for the tabs gives before anything scrolls
                Spacer(Modifier.weight(1f, false).height(DrawTabRoom))
            }
            // nothing behind takes a tap while a fill spreads or a popup is up, a tap there only nudges the popup
            if (state.isSpreading.value || !isFree)
            {
                Box(Modifier.matchParentSize().clearAndSetSemantics { }.clickable(remember { MutableInteractionSource() }, null, true, null, null, ::nudge))
            }
        }
        // a popup stays drawn until it has shrunk or faded all the way out
        if (state.popup.intValue == DRAW_POPUP_LEAVE || state.leaveShow.value > 0f)
        {
            DrawLeavePopup(state, pixels.value, ::askName, ::closePopup, ::leave)
        }
        if (state.popup.intValue == DRAW_POPUP_SAVE || state.saveShow.value > 0f)
        {
            DrawSavePopup(state, pixels.value, ::type, ::closePopup, ::save)
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
private fun DrawTopRow(state: DrawScreenState, line: String, canUndo: Boolean, isFree: Boolean, onBack: () -> Unit, onUndo: () -> Unit)
{
    // bloop and his bubble leave their seat while he sits on the leave popup, their spot is kept
    var seat = 1f
    if (state.popup.intValue == DRAW_POPUP_LEAVE || state.leaveShow.value > 0f)
    {
        seat = 0f
    }
    Row(Modifier.fillMaxWidth().heightIn(DrawTopRowHeight).padding(DrawMargin, 0.dp), Arrangement.spacedBy(DrawGap), Alignment.CenterVertically)
    {
        // the bubble hugs its words and takes the room that's left
        val bubble = Modifier
            .weight(1f)
            .wrapContentWidth(Alignment.Start)
            .alpha(seat)
            .drawBehind { drawRaised(this, GridCardWhite, GridCardEdge, DrawBubbleRadius, DrawPieceEdge) }
            .padding(DrawBubblePadX, DrawBubblePadY)
        // back says its name to talkback and notes where it sits, the leave popup grows out of it
        val backLook = Modifier
            .size(DrawBackSize)
            .semantics { contentDescription = DRAW_LABEL_BACK }
            .onGloballyPositioned { state.backCenter = it.localToRoot(Offset(it.size.width / 2f, it.size.height / 2f)) }
        DrawButton(DRAW_BACK_ARROW, DrawArrowStyle, DRAW_LOOK_WHITE, false, isFree, backLook, onBack)
        Image(painterResource(R.drawable.bloop_clapping), null, Modifier.size(DrawBloopSize).alpha(seat))
        Text(line, bubble, style = DrawTextStyle)
        DrawButton(DRAW_WORD_UNDO, DrawTextStyle, DRAW_LOOK_WHITE, false, canUndo, Modifier.size(DrawUndoWidth, DrawBackSize), onUndo)
    }
}

// the popup that asks before a kid leaves with a drawing, bloop sits on its top edge
@Composable
private fun DrawLeavePopup(state: DrawScreenState, pixels: List<Int>, onSave: () -> Unit, onKeep: () -> Unit, onLeave: () -> Unit)
{
    val button = Modifier.fillMaxWidth().height(DrawPopButtonHeight)
    // bloop sits on the top edge, a tap on him goes nowhere, the same as a tap on the popup
    val sit = Modifier.zIndex(1f).offset(0.dp, DrawSitDrop).size(DrawSitSize).pointerInput(Unit)
    {
    }
    Column(getPopupLayer(state, state.leaveShow.value, state.backCenter), Arrangement.Top, Alignment.CenterHorizontally)
    {
        Image(painterResource(R.drawable.bloop_watching), null, sit)
        Column(getPopupShell(state, Modifier), Arrangement.spacedBy(DrawPopupGap), Alignment.CenterHorizontally)
        {
            Text(DRAW_SAY_UNSAVED, style = DrawTitleStyle)
            DrawPicture(pixels)
            Text(DRAW_SAY_LOSE, style = DrawLineStyle)
            // the Save my block button notes where it sits, the save popup grows out of it
            val saveLook = button.onGloballyPositioned { state.saveMineCenter = it.localToRoot(Offset(it.size.width / 2f, it.size.height / 2f)) }
            DrawButton(DRAW_WORD_SAVE_MINE, DrawPopStyle, DRAW_LOOK_TEAL, false, true, saveLook, onSave)
            DrawButton(DRAW_WORD_KEEP, DrawPopStyle, DRAW_LOOK_OUTLINE, false, true, button, onKeep)
            DrawButton(DRAW_WORD_LEAVE, DrawPopStyle, DRAW_LOOK_QUIET, false, true, button, onLeave)
        }
    }
}

// the popup that names the block and saves it, with the picture, the name field, its line, Not yet and Save
@Composable
private fun DrawSavePopup(state: DrawScreenState, pixels: List<Int>, onType: (String) -> Unit, onNotYet: () -> Unit, onSave: () -> Unit)
{
    val focus = remember { FocusRequester() }
    // the keyboard opens with the popup
    LaunchedEffect(Unit)
    {
        focus.requestFocus()
    }
    // with no name the field turns coral as its line comes in, and shakes
    var coral = 0f
    if (state.line.value == DRAW_SAY_NO_NAME)
    {
        coral = state.lineShow.value
    }
    val fieldColor = lerp(BloopCreamDeep, BloopCoral, coral)
    val field = Modifier
        .fillMaxWidth()
        .height(DrawBackSize)
        .offset(state.shake.value.dp, 0.dp)
        .drawBehind { drawRaised(this, fieldColor, fieldColor, DrawPieceRadius, 0.dp) }
        .focusRequester(focus)
    Column(getPopupShell(state, getPopupLayer(state, state.saveShow.value, state.saveOrigin)), Arrangement.spacedBy(DrawPopupGap), Alignment.CenterHorizontally)
    {
        Text(DRAW_SAY_NAME, style = DrawTitleStyle)
        DrawPicture(pixels)
        // the padding sits inside the field, so a tap anywhere on it lands in the name
        BasicTextField(state.name.value, onType, field, textStyle = DrawTextStyle, singleLine = true, cursorBrush = SolidColor(BloopDark), decorationBox = { text ->
            Box(Modifier.padding(DrawBubblePadX, 0.dp), Alignment.CenterStart)
            {
                text()
            }
        })
        // the line's spot is always kept so the popup never jumps, its words go once it has faded
        var line = ""
        if (state.lineShow.value > 0f)
        {
            line = state.line.value
        }
        Text(line, Modifier.heightIn(DrawLineHeight).alpha(state.lineShow.value), style = DrawLineStyle)
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(DrawGap))
        {
            val button = Modifier.weight(1f).height(DrawPopButtonHeight)
            DrawButton(DRAW_WORD_NOT_YET, DrawPopStyle, DRAW_LOOK_OUTLINE, false, !state.isSaving.value, button, onNotYet)
            DrawButton(DRAW_WORD_SAVE, DrawPopStyle, DRAW_LOOK_TEAL, false, !state.isSaving.value, button, onSave)
        }
    }
}

// the drawing as a small picture, the real grid shrunk with touch off and talkback skipping it
@Composable
private fun DrawPicture(pixels: List<Int>)
{
    // the grid asks for its three handlers, with touch off it never calls them
    fun skipSquare(index: Int)
    {
    }

    // the same, for a stroke's start and end
    fun skipStroke()
    {
    }

    Box(Modifier.size(DrawPictureSize).clip(RoundedCornerShape(DrawPictureRadius)).clearAndSetSemantics { })
    {
        DrawGrid(pixels, 0, GRID_TOUCH_OFF, ::skipSquare, ::skipStroke, ::skipStroke, Modifier.scale(DrawPictureSize / GridMaxWidth).requiredSize(GridMaxWidth))
    }
}

// returns the full screen layer a popup sits centered in, it grows out of its opener or only fades with Remove animations on
private fun getPopupLayer(state: DrawScreenState, amount: Float, origin: Offset): Modifier
{
    val layer = Modifier.fillMaxSize().graphicsLayer {
        if (state.isMotionOff)
        {
            alpha = amount
        }
        else
        {
            scaleX = amount
            scaleY = amount
            transformOrigin = TransformOrigin(origin.x / size.width, origin.y / size.height)
        }
    }
    return layer.safeDrawingPadding().wrapContentSize(Alignment.Center)
}

// returns a popup's own look on modifier, white on a dark edge, it pulses on a tap behind it and swallows a tap on it
private fun getPopupShell(state: DrawScreenState, modifier: Modifier): Modifier
{
    val shell = modifier
        .padding(DrawPopupSide, 0.dp)
        .widthIn(0.dp, DrawPopupWidth)
        .scale(state.pulse.value)
        .drawBehind { drawRaised(this, GridCardWhite, BloopDark, DrawPopupPad, GridEdgeHeight) }
        .pointerInput(Unit)
        {
        }
    return shell.padding(DrawPopupPad)
}

// the white tray under the grid, 8 pots in 2 rows of 4
@Composable
private fun DrawTray(pot: MutableIntState, isFree: Boolean, modifier: Modifier)
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
                    DrawPot(row * DRAW_POTS_PER_ROW + col, pot, isFree)
                }
            }
        }
    }
}

// one paint pot, a tap picks it and the picked one wears the ring, behind a popup it's plain gray
@Composable
private fun DrawPot(index: Int, pot: MutableIntState, isFree: Boolean)
{
    val source = remember { MutableInteractionSource() }
    val isPicked = pot.intValue == index
    var color = getSquareColor(DrawPotColors[index])
    if (!isFree)
    {
        color = BloopIconGray
    }
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
        .drawBehind { drawPot(this, color, isPicked && isFree) }
        .selectable(isPicked, source, null, isFree, Role.RadioButton, ::pick)
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
private fun DrawButton(text: String, style: TextStyle, look: Int, isPicked: Boolean, isEnabled: Boolean, modifier: Modifier, onClick: () -> Unit)
{
    val source = remember { MutableInteractionSource() }
    var fill = GridCardWhite
    var edge = GridCardEdge
    var radius = DrawPieceRadius
    var rest = DrawPieceEdge
    // the popup looks are rounder, quiet stays on linen, outline sits on teal, teal is on a darker and taller edge
    if (look != DRAW_LOOK_WHITE)
    {
        radius = GridCardRadius
    }
    if (look == DRAW_LOOK_OUTLINE)
    {
        edge = BloopTeal
    }
    if (look == DRAW_LOOK_TEAL)
    {
        fill = BloopTeal
        edge = DrawTealEdge
        rest = GridEdgeHeight
    }
    var lift = rest
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
        .offset(0.dp, rest - lift)
        .drawBehind { drawRaised(this, shownFill.value, shownEdge.value, radius, lift) }
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
