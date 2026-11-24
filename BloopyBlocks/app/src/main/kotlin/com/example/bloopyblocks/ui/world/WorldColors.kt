package com.example.bloopyblocks.ui.world

import androidx.compose.ui.graphics.Color

// 0 means an empty square, so there is nothing to draw
fun pixelToColor(pixel: Int): Color? = if (pixel == 0) null else Color(pixel)