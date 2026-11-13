package com.example.bloopyblocks.world

import com.example.bloopyblocks.data.World

class WorldGrid(val width: Int = 12, val height: Int = 8) {

    init { require(width > 0 && height > 0) { "Grid must have a positive size" } }

    // One flat list, row by row. "" means an empty cell.
    private val cells = MutableList(width * height) { "" }
    private val undoStack = ArrayDeque<List<String>>()
    private val redoStack = ArrayDeque<List<String>>()

    private fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height
    private fun index(x: Int, y: Int) = y * width + x

    // Returns the block id at this cell, or "" if empty or out of bounds
    fun get(x: Int, y: Int): String = if (inBounds(x, y)) cells[index(x, y)] else ""

    // Returns true only if the grid actually changed
    fun place(x: Int, y: Int, blockId: String): Boolean {
        if (!inBounds(x, y) || blockId.isEmpty() || get(x, y) == blockId) return false
        saveForUndo()
        cells[index(x, y)] = blockId
        return true
    }

    fun erase(x: Int, y: Int): Boolean {
        if (!inBounds(x, y) || get(x, y).isEmpty()) return false
        saveForUndo()
        cells[index(x, y)] = ""
        return true
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        redoStack.addLast(cells.toList())
        restore(undoStack.removeLast())
        return true
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        undoStack.addLast(cells.toList())
        restore(redoStack.removeLast())
        return true
    }

    // Flatten into the model Tyreece's save code will store
    fun toWorld(id: String, name: String) =
        World(id = id, name = name, width = width, height = height, cells = cells.toList())

    private fun saveForUndo() {
        undoStack.addLast(cells.toList())
        if (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun restore(snapshot: List<String>) {
        for (i in snapshot.indices) cells[i] = snapshot[i]
    }

    companion object {
        // Rebuild a grid from a saved World (an empty grid if the data looks wrong)
        fun fromWorld(world: World): WorldGrid {
            val grid = WorldGrid(world.width, world.height)
            if (world.cells.size == world.width * world.height) {
                for (i in world.cells.indices) grid.cells[i] = world.cells[i]
            }
            return grid
        }
    }
}