package com.example.bloopyblocks.ui.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloopyblocks.R
import com.example.bloopyblocks.ui.theme.BloopyBlocksTheme

//waiting on engy for shared ui colors
private val BackgroundColor: Color = Color(0xFFFFF9EE)
private val TextColor: Color = Color(0xFF24324A)
private val AccentColor: Color = Color(0xFF42C5B5)
private val MutedColor: Color = Color(0xFF5B6678)
private val LinenColor: Color = Color(0xFFE7E0D4)
private val CreamDeepColor: Color = Color(0xFFFFF3D6)
private val IconGrayColor: Color = Color(0xFFD9DEE7)

//waiting on engy to move the fonts into the shared theme
private val TitleFont: FontFamily = FontFamily(
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)
private val BodyFont: FontFamily = FontFamily(
    Font(R.font.nunito_bold, weight = FontWeight.Bold)
)

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
    //waiting on engy for navigation
    onBuild: () -> Unit,
    onDraw: () -> Unit,
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
        fontFamily = BodyFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextColor
    )
    val galleryTypography: Typography = MaterialTheme.typography.copy(
        headlineSmall = TextStyle(
            fontFamily = TitleFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = TextColor
        ),
        bodyMedium = bodyStyle,
        labelMedium = bodyStyle,
        labelLarge = TextStyle(
            fontFamily = TitleFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextColor
        )
    )

    MaterialTheme(typography = galleryTypography) {
        Scaffold(
            modifier = modifier,
            containerColor = BackgroundColor,
            contentColor = TextColor,
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
                    Column {
                        Text(
                            text = "My Blocks",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = countText,
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedColor
                        )
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
            color = LinenColor,
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
    val cardColor: Color = if (isSelected) CreamDeepColor else Color.White

    Box(
        modifier = Modifier.fillMaxWidth().flatShadow(radius = 16.dp, depth = 4.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().semantics { selected = isSelected },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
                contentColor = TextColor
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
                val pixelColor: Color = if (pixel == 0) LinenColor else Color(pixel)

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
                    color = AccentColor,
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
            contentColor = TextColor,
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

//waiting on engy for home and ui setup to attach
@Composable
private fun GalleryNavigation(onBuild: () -> Unit, onDraw: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = false,
            onClick = onBuild,
            icon = {
                Box(Modifier.size(22.dp).background(IconGrayColor, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("Build", style = MaterialTheme.typography.bodyMedium)
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onDraw,
            icon = {
                Box(Modifier.size(22.dp).background(IconGrayColor, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("Draw", style = MaterialTheme.typography.bodyMedium)
            }
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Box(Modifier.size(22.dp).background(AccentColor, RoundedCornerShape(6.dp)))
            },
            label = {
                Text("My Blocks", style = MaterialTheme.typography.bodyMedium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = TextColor,
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
            onDraw = {}
        )
    }
}


