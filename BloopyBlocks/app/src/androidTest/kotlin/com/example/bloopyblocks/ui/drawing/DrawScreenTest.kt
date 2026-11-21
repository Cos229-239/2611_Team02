package com.example.bloopyblocks.ui.drawing

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// taps and swipes on the whole draw screen, run on the medium phone emulator, a very short one hides Fill under the fold
class DrawScreenTest
{
    // a pretend block store that keeps what the screen saves
    private class FakeBlockRepository : BlockRepository
    {
        // what the screen saved, and the block list it never reads
        val saved = mutableListOf<BlockEntity>()
        override val allBlocks = flowOf(emptyList<BlockEntity>())

        // keeps the block
        override suspend fun insertBlock(block: BlockEntity)
        {
            saved.add(block)
        }

        // the screen never updates a block
        override suspend fun updateBlock(block: BlockEntity)
        {
        }

        // the screen never deletes a block
        override suspend fun deleteBlock(block: BlockEntity)
        {
        }

        // always hands back null, the screen only asks after a save that threw
        override suspend fun getBlockById(id: String): BlockEntity?
        {
            return null
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // three pot colors as the plain numbers from the design
    private val teal = Color(0xFF42C5B5)
    private val yellow = Color(0xFFFFD166)
    private val coral = Color(0xFFFF7A66)

    // what the screen remembers, where it saves, and how many times it asked to go home
    private val state = DrawScreenState()
    private val store = FakeBlockRepository()
    private var homeTrips = 0

    // counts a trip home, the screen calls this
    private fun goHome()
    {
        homeTrips++
    }

    // taps the middle of one square
    private fun tapSquare(index: Int)
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(index, width))
            up()
        }
    }

    // returns the color a square shows, read off a picture of the grid
    private fun readSquareColor(index: Int): Color
    {
        val picture = composeTestRule.onNodeWithContentDescription(GRID_LABEL).captureToImage()
        val center = getSquareCenter(index, picture.width)
        return picture.toPixelMap()[center.x.toInt(), center.y.toInt()]
    }

    // puts the draw screen up
    @Before
    fun setup()
    {
        composeTestRule.setContent {
            DrawScreen(listOf(store), state, ::goHome)
        }
    }

    // a pot tap picks the color, a tap on the grid paints with it, and erase paints empty
    @Test
    fun potsPaintAndErase()
    {
        composeTestRule.onNodeWithContentDescription(DrawPotNames[0]).assertIsSelected()
        composeTestRule.onNodeWithText(DRAW_SAY_OPEN).assertExists()
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(DrawPotNames[2]).performClick()
        composeTestRule.onNodeWithContentDescription(DrawPotNames[2]).assertIsSelected()
        composeTestRule.onNodeWithContentDescription(DrawPotNames[0]).assertIsNotSelected()
        tapSquare(0)
        assertEquals(coral, readSquareColor(0))
        composeTestRule.onNodeWithText(DRAW_SAY_GOING).assertExists()
        composeTestRule.onNodeWithContentDescription(DRAW_WORD_ERASE).performClick()
        tapSquare(0)
        assertEquals(GridEmptySquare, readSquareColor(0))
    }

    // undo takes back one whole stroke, then it has nothing left
    @Test
    fun undoTakesBackOneStroke()
    {
        composeTestRule.onNodeWithContentDescription(GRID_LABEL).performTouchInput {
            down(getSquareCenter(0, width))
            moveTo(getSquareCenter(7, width))
            up()
        }
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).performClick()
        assertEquals(GridEmptySquare, readSquareColor(0))
        assertEquals(GridEmptySquare, readSquareColor(7))
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).assertIsNotEnabled()
        composeTestRule.onNodeWithText(DRAW_SAY_OPEN).assertExists()
    }

    // a fill takes the whole patch with the picked pot, then Fill turns itself off
    @Test
    fun fillTakesThePatchThenTurnsOff()
    {
        tapSquare(0)
        tapSquare(1)
        composeTestRule.onNodeWithText(DRAW_WORD_FILL).performClick()
        // a pot tap leaves Fill on
        composeTestRule.onNodeWithContentDescription(DrawPotNames[1]).performClick()
        composeTestRule.onNodeWithText(DRAW_WORD_FILL).assertIsSelected()
        composeTestRule.onNodeWithText(DRAW_SAY_FILL).assertExists()
        tapSquare(0)
        assertEquals(yellow, readSquareColor(0))
        assertEquals(yellow, readSquareColor(1))
        composeTestRule.onNodeWithText(DRAW_WORD_FILL).assertIsNotSelected()
        composeTestRule.onNodeWithText(DRAW_SAY_FILLED).assertExists()
        tapSquare(20)
        composeTestRule.onNodeWithText(DRAW_SAY_GOING).assertExists()
        // undo the stroke and the fill and teal is back, one more takes back the tap on square 1
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).performClick()
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).performClick()
        assertEquals(teal, readSquareColor(1))
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).performClick()
        assertEquals(teal, readSquareColor(0))
        assertEquals(GridEmptySquare, readSquareColor(1))
    }

    // on an empty grid the back button and the phone's back both go home
    @Test
    fun backGoesHomeOnAnEmptyGrid()
    {
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).performClick()
        assertEquals(1, homeTrips)
        Espresso.pressBack()
        assertEquals(2, homeTrips)
    }

    // with a drawing back asks first, the phone's back too, every other button keeps the drawing, and only Leave goes home
    @Test
    fun leavePopupKeepsTheDrawing()
    {
        tapSquare(0)
        Espresso.pressBack()
        composeTestRule.onNodeWithText(DRAW_SAY_UNSAVED).assertExists()
        Espresso.pressBack()
        composeTestRule.onNodeWithText(DRAW_SAY_UNSAVED).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).performClick()
        composeTestRule.onNodeWithText(DRAW_WORD_KEEP).performClick()
        composeTestRule.onNodeWithText(DRAW_SAY_UNSAVED).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).performClick()
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE_MINE).performClick()
        composeTestRule.onNodeWithText(DRAW_SAY_NAME).assertExists()
        composeTestRule.onNodeWithText(DRAW_WORD_NOT_YET).performClick()
        composeTestRule.onNodeWithText(DRAW_SAY_NAME).assertDoesNotExist()
        assertEquals(teal, readSquareColor(0))
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).performClick()
        assertEquals(0, homeTrips)
        composeTestRule.onNodeWithText(DRAW_WORD_LEAVE).performClick()
        assertEquals(1, homeTrips)
    }

    // no name saves nothing and asks kindly, a name saves one block even on a double tap, then home
    @Test
    fun saveNeedsANameThenSavesOnce()
    {
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE_BLOCK).assertIsNotEnabled()
        tapSquare(0)
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE_BLOCK).performClick()
        composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE).performClick()
        composeTestRule.onNodeWithText(DRAW_SAY_NO_NAME).assertExists()
        assertEquals(0, store.saved.size)
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Heart")
        composeTestRule.onNodeWithText(DRAW_SAY_NO_NAME).assertDoesNotExist()
        // two taps with no frame between them, so only the screen's own check stops the second save
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE).performTouchInput {
            down(center)
            up()
            down(center)
            up()
        }
        composeTestRule.waitForIdle()
        assertEquals(1, store.saved.size)
        assertEquals("Heart", store.saved[0].name)
        assertEquals(teal, Color(store.saved[0].pixels[0]))
        assertEquals(1, homeTrips)
    }

    // behind a popup the buttons are off, and a tap there only pulses the popup
    @Test
    fun behindAPopupNothingWorks()
    {
        tapSquare(0)
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).performClick()
        composeTestRule.onNodeWithContentDescription(DRAW_LABEL_BACK).assertIsNotEnabled()
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).assertIsNotEnabled()
        composeTestRule.onNodeWithText(DRAW_WORD_FILL).assertIsNotEnabled()
        composeTestRule.onNodeWithText(DRAW_WORD_SAVE_BLOCK).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(DrawPotNames[2]).assertIsNotEnabled()
        composeTestRule.onNodeWithText(DRAW_WORD_UNDO).performClick()
        assertEquals(1, state.pulses.intValue)
    }
}
