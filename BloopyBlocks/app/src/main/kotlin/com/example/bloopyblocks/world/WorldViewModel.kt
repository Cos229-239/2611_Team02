package com.example.bloopyblocks.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloopyblocks.data.RoomWorldRepository
import com.example.bloopyblocks.data.World
import com.example.bloopyblocks.data.WorldMode
import com.example.bloopyblocks.data.WorldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID

class WorldViewModel(private val worldRepository: WorldRepository?=null, var grid: WorldGrid = WorldGrid()) : ViewModel() {

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
    // All worlds saved in the repository
    private val _savedWorlds = MutableStateFlow<List<World>>(emptyList())
    val savedWorlds: StateFlow<List<World>> = _savedWorlds

    // The world currently being edited
    private val _currentWorldId = MutableStateFlow<String?>(null)
    val currentWorldId: StateFlow<String?> = _currentWorldId

    private val _currentWorldName = MutableStateFlow("")
    val currentWorldName: StateFlow<String> = _currentWorldName

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    init {
        worldRepository?.let { repository ->
            viewModelScope.launch {
                repository.allWorlds.collect { worlds ->
                    _savedWorlds.value = worlds
                }
            }
        }
    }



    fun onCellTapped(x: Int, y: Int) {
        val changed = when (_mode.value) {
            WorldMode.PLACE -> _selectedBlockId.value?.let { grid.place(x, y, it) } ?: false
            WorldMode.TAKE_OUT -> grid.erase(x, y)
        }
        if (changed) _cells.value = grid.snapshot()
    }

    fun undo() { if (grid.undo()) _cells.value = grid.snapshot() }
    fun redo() { if (grid.redo()) _cells.value = grid.snapshot() }
    // Save the current grid to Room and Firestore
    fun saveWorld(name: String) {
        val repository = worldRepository

        if (repository == null) {
            _saveError.value = "World repository is not available."
            return
        }

        val worldName = name.trim()

        if (worldName.isEmpty()) {
            _saveError.value = "Please enter a world name."
            return
        }

        viewModelScope.launch {
            try {
                val world = grid.toWorld(
                    id = _currentWorldId.value ?: UUID.randomUUID().toString(),
                    name = worldName
                )

                repository.insertWorld(world)

                _currentWorldId.value = world.id
                _currentWorldName.value = world.name
                _saveError.value = null

            } catch (exception: Exception) {
                _saveError.value = exception.message ?: "Could not save world."
            }
        }
    }

    // Load a previously saved world
    fun loadWorld(worldId: String) {
        val world = _savedWorlds.value.firstOrNull {
            it.id == worldId
        } ?: return

        grid = WorldGrid.fromWorld(world)

        _cells.value = grid.snapshot()
        _currentWorldId.value = world.id
        _currentWorldName.value = world.name
        _saveError.value = null
    }

    fun deleteWorld(worldId: String) {
        val repository = worldRepository ?: return

        viewModelScope.launch {
            try {
                val world = _savedWorlds.value.firstOrNull {
                    it.id == worldId
                } ?: return@launch

                repository.deleteWorld(world)

// If the deleted world was currently open, clear its ID
                if (_currentWorldId.value == worldId) {
                    _currentWorldId.value = null
                    _currentWorldName.value = ""
                }

                _saveError.value = null

            } catch (exception: Exception) {
                _saveError.value =
                    exception.message ?: "Could not delete world."
            }
        }
    }
}