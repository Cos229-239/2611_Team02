package com.example.bloopyblocks.ui.world

import com.example.bloopyblocks.data.Block

// Stand-in blocks until the gallery hands over real ones
val sampleBlocks = listOf(
    Block("grass", "Grass", List(64) { if (it < 16) 0xFF7CCB6B.toInt() else 0xFFC98A5B.toInt() }),
    Block("sun", "Sun", List(64) { 0xFFFFD166.toInt() }),
    Block("brick", "Brick", List(64) { 0xFFFF7A66.toInt() }),
    Block("water", "Water", List(64) { 0xFF6C8CFF.toInt() })
)