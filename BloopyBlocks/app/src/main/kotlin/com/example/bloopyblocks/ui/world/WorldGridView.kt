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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bloopyblocks.data.Block
import kotlin.math.roundToInt

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

        // draw the blocks first
        for (y in 0 until height) {
            for (x in 0 until width) {
                val block = blocks[cells.getOrNull(y * width + x) ?: ""]
                if (block != null) {
                    drawBlock(block, x * cellWidth, y * cellHeight, cellWidth, cellHeight)
                }
            }
        }

        // then draw each grid line once, snapped to whole screen pixels so they all look the same
        val lineColor = Color(0x33000000)
        val linePx = 1.dp.toPx().roundToInt().coerceAtLeast(1).toFloat()
        for (i in 0..width) {
            val lineX = (i * cellWidth).roundToInt().toFloat().coerceAtMost(size.width - linePx)
            drawRect(lineColor, Offset(lineX, 0f), Size(linePx, size.height))
        }
        for (j in 0..height) {
            val lineY = (j * cellHeight).roundToInt().toFloat().coerceAtMost(size.height - linePx)
            drawRect(lineColor, Offset(0f, lineY), Size(size.width, linePx))
        }
    }
}

// Paints one block's 64 pixels, skipping empty squares so the sky shows through
internal fun DrawScope.drawBlock(block: Block, left: Float, top: Float, cellWidth: Float, cellHeight: Float) {
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            val color = pixelToColor(block.pixels.getOrElse(row * 8 + col) { 0 })
            if (color != null) {
                // snap every edge to a whole pixel so neighbors share edges with no gaps
                val x0 = (left + col * cellWidth / 8f).roundToInt().toFloat()
                val x1 = (left + (col + 1) * cellWidth / 8f).roundToInt().toFloat()
                val y0 = (top + row * cellHeight / 8f).roundToInt().toFloat()
                val y1 = (top + (row + 1) * cellHeight / 8f).roundToInt().toFloat()
                drawRect(color, Offset(x0, y0), Size(x1 - x0, y1 - y0))
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
    val cells = List(8 * 8) { if (it >= 8 * 7 || it == 8 * 3 + 4) "demo" else "" }

    WorldGridView(
        cells = cells,
        width = 8,
        height = 8,
        blocks = mapOf("demo" to demo),
        onCellTapped = { _, _ -> }
    )
}