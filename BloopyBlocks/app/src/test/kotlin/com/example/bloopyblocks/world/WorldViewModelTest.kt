package com.example.bloopyblocks.world

import com.example.bloopyblocks.data.WorldMode
import org.junit.Assert.assertEquals
import org.junit.Test

class WorldViewModelTest {

    @Test
    fun tapPlacesSelectedBlock() {
        val vm = WorldViewModel(grid = WorldGrid(4, 4))
        vm.selectBlock("blk_1")
        vm.onCellTapped(1, 1)
        assertEquals("blk_1", vm.cells.value[1 * 4 + 1])
    }

    @Test
    fun tapWithNoBlockSelectedDoesNothing() {
        val vm = WorldViewModel(grid = WorldGrid(4, 4))
        vm.onCellTapped(1, 1)
        assertEquals("", vm.cells.value[1 * 4 + 1])
    }

    @Test
    fun takeOutModeErasesTheCell() {
        val vm = WorldViewModel(grid = WorldGrid(4, 4))
        vm.selectBlock("blk_1")
        vm.onCellTapped(2, 2)
        vm.setMode(WorldMode.TAKE_OUT)
        vm.onCellTapped(2, 2)
        assertEquals("", vm.cells.value[2 * 4 + 2])
    }
}