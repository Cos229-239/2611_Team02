package com.example.bloopyblocks

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.bloopyblocks.ui.gallery.GalleryRoute
import com.example.bloopyblocks.ui.main.MainScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloopyblocks.ui.world.WorldScreen
import com.example.bloopyblocks.ui.world.sampleBlocks
import com.example.bloopyblocks.world.WorldViewModel
import com.example.bloopyblocks.ui.welcome.WelcomeScreen
import com.example.bloopyblocks.ui.world.WorldRoute

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Welcome)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry <Welcome>{
          WelcomeScreen(
            onStart = {
              backStack.add(Main)
              backStack.remove(Welcome)
            }
          )
        }
        entry<Main> {
          MainScreen(onItemClick = { navKey -> backStack.add(navKey) })
        }
        entry<Draw> {
          Text("Draw screen...")
        }
        entry<Gallery> {
          GalleryRoute(
            onNewBlock = { backStack.add(Draw) },
            onBlockSelected = {},
            onBuild = { backStack.add(World) },
            onDraw = { backStack.add(Draw) },
            onBackToHome = {
              while (backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
          )
        }
        entry<World> {
          WorldRoute()
        }
      },
  )
}
