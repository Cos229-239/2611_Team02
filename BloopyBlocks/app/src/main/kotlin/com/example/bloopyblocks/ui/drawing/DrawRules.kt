package com.example.bloopyblocks.ui.drawing

import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout

// undo goes back this many steps, and a block's name holds this many letters
const val DRAW_UNDO_MAX = 50
const val DRAW_NAME_MAX = 16

// the erase pot is the last one in the tray
const val DRAW_ERASE_POT = 7

// how long a save can take before the phone copy gets checked, in ms
private const val DRAW_SAVE_WAIT_MS = 5000L

// the 8 pots in tray order, teal to dark, then 0 for erase
val DrawPotColors = listOf(
    0xFF42C5B5.toInt(), 0xFFFFD166.toInt(), 0xFFFF7A66.toInt(), 0xFF6C8CFF.toInt(),
    0xFFC98A5B.toInt(), 0xFF7CCB6B.toInt(), 0xFF24324A.toInt(), 0
)

// all 64 squares empty, where every drawing starts
val DrawEmptyGrid = IntArray(GRID_SQUARE_COUNT).toList()

// returns a copy with the square at index painted, off the grid changes nothing
fun paintSquare(pixels: List<Int>, index: Int, color: Int): List<Int>
{
    val painted = pixels.toMutableList()
    if (index >= 0 && index < painted.size)
    {
        painted[index] = color
    }
    return painted
}

// returns steps with before added on the end, each step a whole grid
fun addUndoStep(steps: List<List<Int>>, before: List<Int>, after: List<Int>): List<List<Int>>
{
    // a stroke that changed nothing saves no step
    if (before == after)
    {
        return steps
    }
    val added = steps.toMutableList()
    added.add(before)
    // past the limit the oldest step drops off the front
    if (added.size > DRAW_UNDO_MAX)
    {
        added.removeAt(0)
    }
    return added
}

// true when any square has paint, a fully erased grid counts as empty
fun hasDrawing(pixels: List<Int>): Boolean
{
    return pixels != DrawEmptyGrid
}

// returns typed with both ends trimmed, only spaces comes back empty
fun cleanBlockName(typed: String): String
{
    return typed.trim()
}

// returns typed cut down to DRAW_NAME_MAX letters
fun limitBlockName(typed: String): String
{
    return typed.take(DRAW_NAME_MAX)
}

// saves the block, true when it lands on the phone, an empty list is false
suspend fun saveBlock(repositories: List<BlockRepository>, block: BlockEntity): Boolean
{
    if (repositories.isEmpty())
    {
        return false
    }
    try
    {
        // the save gets a time limit, the cloud copy can hang offline
        withTimeout(DRAW_SAVE_WAIT_MS)
        {
            repositories[0].insertBlock(block)
        }
        return true
    }
    catch (e: Exception)
    {
        // a cancel from outside gets passed on, a throw or the time limit moves on
        currentCoroutineContext().ensureActive()
    }
    // the phone copy gets written first, so a block found there still counts as saved
    try
    {
        return repositories[0].getBlockById(block.id) != null
    }
    catch (e: Exception)
    {
        currentCoroutineContext().ensureActive()
        return false
    }
}
