package com.example.bloopyblocks.ui.drawing

import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import java.io.Serializable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// painting, undo steps, anything drawn, the block's name, and the save
class DrawRulesTest
{
    // a pretend block store that keeps what's saved and can throw or hang
    private class FakeBlockRepository : BlockRepository
    {
        // every block saved so far
        val saved = mutableListOf<BlockEntity>()

        // flip one so a save throws before it lands, or lands then hangs
        var throwBeforeSave = false
        var hangAfterSave = false

        // the save never reads this
        override val allBlocks: Flow<List<BlockEntity>> = flowOf(emptyList())

        // saves the block, a switch can throw first or hang after
        override suspend fun insertBlock(block: BlockEntity)
        {
            if (throwBeforeSave)
            {
                throw Exception()
            }
            saved.add(block)
            if (hangAfterSave)
            {
                awaitCancellation()
            }
        }

        // the save never updates a block
        override suspend fun updateBlock(block: BlockEntity)
        {
        }

        // the save never deletes one either
        override suspend fun deleteBlock(block: BlockEntity)
        {
        }

        // returns the saved block with this id, or null like the real one
        override suspend fun getBlockById(id: String): BlockEntity?
        {
            for (i in 0 until saved.size)
            {
                if (saved[i].id == id)
                {
                    return saved[i]
                }
            }
            return null
        }
    }

    // teal from the first pot, and a grid with square 0 painted teal
    private val teal = DrawPotColors[0]
    private val painted = paintSquare(DrawEmptyGrid, 0, teal)

    // the block the save tests use, and the fake it goes to
    private val block = BlockEntity("block-1", "Heart", painted)
    private val fake = FakeBlockRepository()

    // the first step holds the grid from before the stroke, a stroke that changed nothing adds no step
    @Test
    fun undoStepKeepsTheGridFromBefore()
    {
        assertEquals(listOf(DrawEmptyGrid), addUndoStep(emptyList(), DrawEmptyGrid, painted))
        assertEquals(listOf(DrawEmptyGrid), addUndoStep(listOf(DrawEmptyGrid), painted, painted))
    }

    // past 50 the oldest step drops, and the steps can be kept for a phone turn
    @Test
    fun undoStopsAt50()
    {
        var steps = addUndoStep(emptyList(), painted, DrawEmptyGrid)
        for (i in 0 until DRAW_UNDO_MAX)
        {
            steps = addUndoStep(steps, DrawEmptyGrid, painted)
        }
        assertEquals(50, steps.size)
        assertEquals(DrawEmptyGrid, steps[0])
        assertTrue(steps is Serializable)
    }

    // an empty grid has no drawing, a painted square does, erasing takes it back
    @Test
    fun erasedDrawingIsEmpty()
    {
        assertFalse(hasDrawing(DrawEmptyGrid))
        assertTrue(hasDrawing(painted))
        assertFalse(hasDrawing(paintSquare(painted, 0, DrawPotColors[DRAW_ERASE_POT])))
    }

    // painting changes one square, and a square off the grid changes nothing
    @Test
    fun paintChangesOneSquare()
    {
        assertEquals(0, painted[1])
        assertEquals(teal, paintSquare(DrawEmptyGrid, 63, teal)[63])
        assertEquals(DrawEmptyGrid, paintSquare(DrawEmptyGrid, -1, teal))
        assertEquals(DrawEmptyGrid, paintSquare(DrawEmptyGrid, GRID_SQUARE_COUNT, teal))
    }

    // ends get trimmed, only spaces is no name, a long name stops at 16
    @Test
    fun nameIsTrimmedAndCut()
    {
        assertEquals("Heart", cleanBlockName("  Heart "))
        assertEquals("", cleanBlockName("   "))
        assertEquals("abcdefghijklmnop", limitBlockName("abcdefghijklmnopqrst"))
    }

    // a good save is true and the block lands as it was handed in
    @Test
    fun goodSaveIsTrue()
    {
        runTest {
            assertTrue(saveBlock(listOf(fake), block))
            assertEquals(listOf(block), fake.saved)
        }
    }

    // the block lands, the cloud hangs, after 5 seconds it still counts as saved
    @Test
    fun hangAfterSavingIsTrue()
    {
        runTest {
            fake.hangAfterSave = true
            assertTrue(saveBlock(listOf(fake), block))
        }
    }

    // a throw before the block lands is false, so is no repository
    @Test
    fun failedSaveIsFalse()
    {
        runTest {
            fake.throwBeforeSave = true
            assertFalse(saveBlock(listOf(fake), block))
            assertFalse(saveBlock(emptyList(), block))
        }
    }
}
