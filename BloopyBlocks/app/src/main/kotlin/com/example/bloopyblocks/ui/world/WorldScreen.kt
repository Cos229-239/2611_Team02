package com.example.bloopyblocks.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bloopyblocks.data.Block
import com.example.bloopyblocks.data.WorldMode
import com.example.bloopyblocks.world.WorldViewModel

@Composable
fun WorldScreen(
    viewModel: WorldViewModel,
    blocks: List<Block>,
    modifier: Modifier = Modifier
) {
    val cells by viewModel.cells.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val selectedId by viewModel.selectedBlockId.collectAsState()
    val blocksById = blocks.associateBy { it.id }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WorldGridView(
            cells = cells,
            width = viewModel.gridWidth,
            height = viewModel.gridHeight,
            blocks = blocksById,
            onCellTapped = viewModel::onCellTapped,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeButton("Place", mode == WorldMode.PLACE) { viewModel.setMode(WorldMode.PLACE) }
            ModeButton("Take out", mode == WorldMode.TAKE_OUT) { viewModel.setMode(WorldMode.TAKE_OUT) }
            OutlinedButton(onClick = viewModel::undo) { Text("Undo") }
            OutlinedButton(onClick = viewModel::redo) { Text("Redo") }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(blocks, key = { it.id }) { block ->
                BlockThumb(block, block.id == selectedId) { viewModel.selectBlock(block.id) }
            }
        }
    }
}

// The selected mode gets a filled button, the other one is outlined
@Composable
private fun ModeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick) { Text(label) }
    else OutlinedButton(onClick = onClick) { Text(label) }
}

// A small tappable picture of one block, with a thicker teal border when it is selected
@Composable
private fun BlockThumb(block: Block, selected: Boolean, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(56.dp)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color(0xFF42C5B5) else Color(0x33000000)
            )
            .clickable(onClick = onClick)
    ) {
        drawBlock(block, 0f, 0f, size.width, size.height)
    }
}

@Preview(showBackground = true)
@Composable
private fun WorldScreenPreview() {
    val blocks = listOf(
        Block("grass", "Grass", List(64) { if (it < 16) 0xFF7CCB6B.toInt() else 0xFFC98A5B.toInt() }),
        Block("sun", "Sun", List(64) { 0xFFFFD166.toInt() }),
        Block("brick", "Brick", List(64) { 0xFFFF7A66.toInt() })
    )
    WorldScreen(viewModel = WorldViewModel(), blocks = blocks)
}
