package com.example.bloopyblocks.world

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldGridTest {

    @Test
    fun placeStoresBlockId() {
        val grid = WorldGrid(4, 4)
        assertTrue(grid.place(1, 2, "blk_1"))
        assertEquals("blk_1", grid.get(1, 2))
    }

    @Test
    fun placeOutOfBoundsDoesNothing() {
        val grid = WorldGrid(4, 4)
        assertFalse(grid.place(9, 9, "blk_1"))
        assertEquals("", grid.get(9, 9))
    }

    @Test
    fun eraseClearsCell() {
        val grid = WorldGrid(4, 4)
        grid.place(0, 0, "blk_1")
        assertTrue(grid.erase(0, 0))
        assertEquals("", grid.get(0, 0))
    }

    @Test
    fun undoAndRedoWork() {
        val grid = WorldGrid(4, 4)
        grid.place(0, 0, "blk_1")
        grid.undo()
        assertEquals("", grid.get(0, 0))
        grid.redo()
        assertEquals("blk_1", grid.get(0, 0))
    }

    @Test
    fun newActionClearsRedo() {
        val grid = WorldGrid(4, 4)
        grid.place(0, 0, "blk_1")
        grid.undo()
        grid.place(1, 1, "blk_2")
        assertFalse(grid.redo())
    }

    @Test
    fun savingAndLoadingKeepsTheGrid() {
        val grid = WorldGrid(4, 4)
        grid.place(3, 3, "blk_9")
        val loaded = WorldGrid.fromWorld(grid.toWorld("w1", "My Castle"))
        assertEquals("blk_9", loaded.get(3, 3))
    }
}