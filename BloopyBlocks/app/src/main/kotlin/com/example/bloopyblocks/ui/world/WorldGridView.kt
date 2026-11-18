package com.example.bloopyblocks.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.bloopyblocks.data.Block

// Draws the world: each cell holds a block id, and the block's 8x8 pixels are painted inside it
@Composable
fun WorldGridView(
    cells: List<String>,
    width: Int,
    height: Int,
    blocks: Map<String, Block>,
    onCellTapped: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .aspectRatio(width.toFloat() / height.toFloat())
            .pointerInput(width, height) {
                detectTapGestures { tap ->
                    val x = (tap.x / (size.width.toFloat() / width)).toInt()
                    val y = (tap.y / (size.height.toFloat() / height)).toInt()
                    onCellTapped(x, y)
                }
            }
    ) {
        val cellWidth = size.width / width
        val cellHeight = size.height / height

        for (y in 0 until height) {
            for (x in 0 until width) {
                val left = x * cellWidth
                val top = y * cellHeight

                // faint outline so players can see where the cells are
                drawRect(
                    color = Color(0x22000000),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight),
                    style = Stroke(width = 1f)
                )

                val block = blocks[cells.getOrNull(y * width + x) ?: ""]
                if (block != null) {
                    drawBlock(block, left, top, cellWidth, cellHeight)
                }
            }
        }
    }
}

// Paints one block's 64 pixels, skipping empty squares so the sky shows through
internal fun DrawScope.drawBlock(block: Block, left: Float, top: Float, cellWidth: Float, cellHeight: Float) {
    val pixelWidth = cellWidth / 8f
    val pixelHeight = cellHeight / 8f
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            val color = pixelToColor(block.pixels.getOrElse(row * 8 + col) { 0 })
            if (color != null) {
                drawRect(
                    color = color,
                    topLeft = Offset(left + col * pixelWidth, top + row * pixelHeight),
                    size = Size(pixelWidth, pixelHeight)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorldGridViewPreview() {
    val yellow = 0xFFFFD166.toInt()
    val brown = 0xFFC98A5B.toInt()
    val demo = Block(id = "demo", name = "Demo", pixels = List(64) { if (it < 32) yellow else brown })
    val cells = List(12 * 8) { if (it >= 12 * 7 || it == 12 * 3 + 5) "demo" else "" }

    WorldGridView(
        cells = cells,
        width = 12,
        height = 8,
        blocks = mapOf("demo" to demo),
        onCellTapped = { _, _ -> }
    )
}