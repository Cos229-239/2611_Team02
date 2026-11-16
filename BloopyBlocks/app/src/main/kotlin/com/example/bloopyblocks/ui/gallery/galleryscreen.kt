package com.example.bloopyblocks.ui.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloopyblocks.R
import com.example.bloopyblocks.ui.theme.*

//waiting on alejandro and tyreece for shared block data
data class GalleryBlock(
    val id: String,
    val name: String,
    val pixels: List<Int>
) {
    init {
        require(pixels.size == 64) { "A block must have 64 pixel colors." }
    }
}

@Composable
fun GalleryScreen(
    //waiting on tyreece for saved blocks
    blocks: List<GalleryBlock>,
    //waiting on engy and alejandro to connect the drawing screen
    onNewBlock: () -> Unit,
    //waiting on jace to use the selected block in the world
    onBlockSelected: (GalleryBlock) -> Unit,
    onBuild: () -> Unit,
    onDraw: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBlockId: String? = null
) {
    val blockCount: Int = blocks.size
    val countText: String = if (blockCount == 1) {
        "1 block created"
    } else {
        "$blockCount blocks created"
    }
    val bodyStyle: TextStyle = TextStyle(
        fontFamily = BloopBodyFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = BloopDark
    )
    val galleryTypography = MaterialTheme.typography.copy(
        headlineSmall = TextStyle(
            fontFamily = BloopTitleFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = BloopDark
        ),
        bodyMedium = bodyStyle,
        labelMedium = bodyStyle,
        labelLarge = TextStyle(
            fontFamily = BloopTitleFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = BloopDark
        )
    )

    MaterialTheme(typography = galleryTypography) {
        Scaffold(
            modifier = modifier,
            containerColor = BloopCream,
            contentColor = BloopDark,
            bottomBar = { GalleryNavigation(onBuild, onDraw) }
        ) { padding: PaddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Blocks",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = countText,
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BloopGray
                            )
                        }
                        IconButton(onClick = onBackToHome) {
                            Text(
                                text = "\u2190",
                                color = BloopDark,
                                fontSize = 24.sp,
                                modifier = Modifier.clearAndSetSemantics {
                                    contentDescription = "Back to Home"
                                }
                            )
                        }
                    }
                }
                items(blocks, key = { block: GalleryBlock -> block.id }) { block: GalleryBlock ->
                    BlockCard(
                        block = block,
                        isSelected = block.id == selectedBlockId,
                        onClick = { onBlockSelected(block) }
                    )
                }
                item { NewBlockCard(onNewBlock) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BloopMessage(blockCount)
                }
            }
        }
    }
}

private fun Modifier.flatShadow(radius: Dp, depth: Dp): Modifier {
    return this.padding(bottom = depth).drawBehind {
        drawRoundRect(
            color = BloopLinen,
            topLeft = Offset(0f, depth.toPx()),
            size = size,
            cornerRadius = CornerRadius(radius.toPx())
        )
    }
}

@Composable
private fun BlockCard(
    block: GalleryBlock,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardColor: Color = if (isSelected) BloopCreamDeep else Color.White

    Box(
        modifier = Modifier.fillMaxWidth().flatShadow(radius = 16.dp, depth = 4.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().semantics { selected = isSelected },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
                contentColor = BloopDark
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BlockArtwork(block)
                Text(
                    text = block.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BlockArtwork(block: GalleryBlock) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.White)
            .semantics {
                contentDescription = "8 by 8 drawing of " + block.name
            }
    ) {
        val cellWidth: Float = size.width / 8f
        val cellHeight: Float = size.height / 8f
        val emptyGap: Float = minOf(cellWidth, cellHeight) / 47f
        val emptyRadius: Float = minOf(cellWidth, cellHeight) * 4f / 47f

        for (row: Int in 0 until 8) {
            for (column: Int in 0 until 8) {
                val pixel: Int = block.pixels[row * 8 + column]
                val inset: Float = if (pixel == 0) emptyGap else 0f
                val radius: Float = if (pixel == 0) emptyRadius else 0f
                val pixelColor: Color = if (pixel == 0) BloopLinen else Color(pixel)

                drawRoundRect(
                    color = pixelColor,
                    topLeft = Offset(
                        column * cellWidth + inset,
                        row * cellHeight + inset
                    ),
                    size = Size(
                        cellWidth - inset * 2f,
                        cellHeight - inset * 2f
                    ),
                    cornerRadius = CornerRadius(radius)
                )
            }
        }
    }
}

@Composable
private fun NewBlockCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .drawBehind {
                val borderWidth: Float = 2.dp.toPx()
                drawRoundRect(
                    color = BloopTeal,
                    topLeft = Offset(borderWidth / 2f, borderWidth / 2f),
                    size = Size(size.width - borderWidth, size.height - borderWidth),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = borderWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                        )
                    )
                )
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "New Block",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

//temporary bloop messages
@Composable
private fun BloopMessage(blockCount: Int) {
    val message: String = if (blockCount == 0) {
        "Draw your first block!\nWhat will you create?"
    } else {
        "Your blocks are ready!\nWhat will you build?"
    }

    Row(
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.bloop),
            contentDescription = "Bloop",
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Fit
        )
        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .flatShadow(radius = 14.dp, depth = 3.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            contentColor = BloopDark,
            shadowElevation = 0.dp
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GalleryNavigation(onBuild: () -> Unit, onDraw: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = false,
            onClick = onBuild,
            icon = {
                Box(Modifier.size(22.dp).background(BloopIconGray, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("Build", style = MaterialTheme.typography.bodyMedium)
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onDraw,
            icon = {
                Box(Modifier.size(22.dp).background(BloopIconGray, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("Draw", style = MaterialTheme.typography.bodyMedium)
            }
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Box(Modifier.size(22.dp).background(BloopTeal, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("My Blocks", style = MaterialTheme.typography.bodyMedium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = BloopDark,
                indicatorColor = Color.Transparent
            )
        )
    }
}
private fun sampleBlock(name: String, rows: List<String>): GalleryBlock {
    val pixels: MutableList<Int> = mutableListOf<Int>()
    for (row: String in rows) {
        for (cell: Char in row) {
            val color: Int = when (cell) {
                'y' -> 0xFFFFD166.toInt()
                'r' -> 0xFFFF7A66.toInt()
                'b' -> 0xFFC98A5B.toInt()
                'd' -> 0xFF24324A.toInt()
                'g' -> 0xFF7CCB6B.toInt()
                'p' -> 0xFF6C8CFF.toInt()
                'c' -> 0xFFFFF3D6.toInt()
                else -> 0
            }
            pixels.add(color)
        }
    }
    return GalleryBlock(id = name, name = name, pixels = pixels)
}

@Preview(showBackground = true, widthDp = 411, heightDp = 914)
@Composable
private fun GalleryScreenPreview() {
    var selectedBlockId: String? by rememberSaveable { mutableStateOf<String?>(null) }
    val blocks: List<GalleryBlock> = listOf(
        sampleBlock("Pizza", listOf(
            "..yyyy..", ".yyyyyy.", ".yryyry.", "yyyyyyyy",
            "yyryyryy", "yyyyyyyy", "bbbbbbbb", "bbbbbbbb"
        )),
        sampleBlock("Cloud", listOf(
            "........", "........", "..ccc...", ".ccccc..",
            "cccccccc", "cccccccc", "..pppp..", "..pppp.."
        )),
        sampleBlock("Flower", listOf(
            "..rrrr..", ".rrrrrr.", ".rryyrr.", "..ryyr..",
            "...gg...", "..gggg..", "...gg...", "...gg..."
        )),
        sampleBlock("Brick", listOf(
            "bbbbbbbb", "bbbbbbbb", "bbddbbdd", "bbbbbbbb",
            "bbbbbbbb", "ddbbddbb", "bbbbbbbb", "bbbbbbbb"
        )),
        sampleBlock("Star", listOf(
            "...yy...", "...yy...", "yyyyyyyy", ".yyyyyy.",
            "..yyyy..", "..yyyy..", ".yy..yy.", ".y....y."
        )),
        sampleBlock("Heart", listOf(
            ".rr..rr.", "rrrrrrrr", "rrrrrrrr", ".rrrrrr.",
            "..rrrr..", "...rr...", "...rr...", "........"
        ))
    )

    BloopyBlocksTheme {
        GalleryScreen(
            blocks = blocks,
            onNewBlock = {},
            onBlockSelected = { block: GalleryBlock -> selectedBlockId = block.id },
            onBuild = {},
            onDraw = {},
            onBackToHome = {},
            selectedBlockId = selectedBlockId
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 914)
@Composable
private fun EmptyGalleryScreenPreview() {
    BloopyBlocksTheme {
        GalleryScreen(
            blocks = emptyList<GalleryBlock>(),
            onNewBlock = {},
            onBlockSelected = {},
            onBuild = {},
            onDraw = {},
            onBackToHome = {}
        )
    }
}
