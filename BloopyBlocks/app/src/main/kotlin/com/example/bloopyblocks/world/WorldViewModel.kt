package com.example.bloopyblocks.world

import androidx.lifecycle.ViewModel
import com.example.bloopyblocks.data.WorldMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WorldViewModel(private val grid: WorldGrid = WorldGrid()) : ViewModel() {

    val gridWidth: Int = grid.width
    val gridHeight: Int = grid.height

    private val _cells = MutableStateFlow(grid.snapshot())
    val cells: StateFlow<List<String>> = _cells

    private val _mode = MutableStateFlow(WorldMode.PLACE)
    val mode: StateFlow<WorldMode> = _mode

    private val _selectedBlockId = MutableStateFlow<String?>(null)
    val selectedBlockId: StateFlow<String?> = _selectedBlockId

    fun setMode(newMode: WorldMode) { _mode.value = newMode }
    fun selectBlock(id: String) { _selectedBlockId.value = id }

    fun onCellTapped(x: Int, y: Int) {
        val changed = when (_mode.value) {
            WorldMode.PLACE -> _selectedBlockId.value?.let { grid.place(x, y, it) } ?: false
            WorldMode.TAKE_OUT -> grid.erase(x, y)
        }
        if (changed) _cells.value = grid.snapshot()
    }

    fun undo() { if (grid.undo()) _cells.value = grid.snapshot() }
    fun redo() { if (grid.redo()) _cells.value = grid.snapshot() }
}