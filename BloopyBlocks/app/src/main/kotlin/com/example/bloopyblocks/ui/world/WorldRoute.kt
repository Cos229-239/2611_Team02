package com.example.bloopyblocks.ui.world

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloopyblocks.BloopyBlocksApplication
import com.example.bloopyblocks.data.Block
import com.example.bloopyblocks.world.WorldViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun WorldRoute(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as BloopyBlocksApplication
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val worldViewModel: WorldViewModel = viewModel()
    var savedBlocks by remember { mutableStateOf<List<Block>>(emptyList()) }

    LaunchedEffect(lifecycle) {
        try {
            application.appContainer.getBlockRepository().allBlocks
                .flowWithLifecycle(lifecycle)
                .collect { saved ->
                    savedBlocks = saved
                        .filter { it.pixels.size == 64 }
                        .map { Block(id = it.id, name = it.name, pixels = it.pixels) }
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // if the repository can't be reached, the stand-in blocks stay on screen
        }
    }

    WorldScreen(
        viewModel = worldViewModel,
        blocks = savedBlocks.ifEmpty { sampleBlocks },
        modifier = modifier
    )
}