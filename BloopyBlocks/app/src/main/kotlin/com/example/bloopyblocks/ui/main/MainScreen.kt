package com.example.bloopyblocks.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.bloopyblocks.Draw
import com.example.bloopyblocks.Gallery
import com.example.bloopyblocks.R
import com.example.bloopyblocks.World
import com.example.bloopyblocks.data.DefaultDataRepository
import com.example.bloopyblocks.ui.theme.BloopBodyFont
import com.example.bloopyblocks.ui.theme.BloopCream
import com.example.bloopyblocks.ui.theme.BloopDark
import com.example.bloopyblocks.ui.theme.BloopGrass
import com.example.bloopyblocks.ui.theme.BloopGray
import com.example.bloopyblocks.ui.theme.BloopSky
import com.example.bloopyblocks.ui.theme.BloopTeal
import com.example.bloopyblocks.ui.theme.BloopTitleFont
import com.example.bloopyblocks.ui.theme.BloopyBlocksTheme

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        MainScreenUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MainScreenUiState.Success -> {
            MainScreen(data = state.data, modifier = modifier, onItemClick = onItemClick)
        }

        is MainScreenUiState.Error -> {
            Text("Error loading data: ${state.throwable.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    data: List<String>,
    modifier: Modifier = Modifier,
    onItemClick: (NavKey) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = BloopCream,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontFamily = BloopTitleFont) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BloopCream,
                    titleContentColor = BloopDark
                )
            )
        }
    ) { innerPadding ->

        // App Content

        Column(
            modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "My World",
                fontSize = 28.sp,
                fontFamily = BloopTitleFont,
                fontWeight = FontWeight.SemiBold,
                color = BloopDark,
                modifier = Modifier.padding(24.dp, 16.dp)
            )
            Text(
                text = "Welcome back, builder!",
                fontSize = 14.sp,
                fontFamily = BloopBodyFont,
                color = BloopGray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Box(
                modifier = Modifier
                  .padding(horizontal = 24.dp)
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(BloopSky),
                contentAlignment = Alignment.BottomCenter
            ) {

                Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(40.dp)
                      .background(BloopGrass)
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.bloop),
                    contentDescription = "Bloop",
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = "Your world missed you!\nReady to build more?",
                    fontSize = 13.sp,
                    fontFamily = BloopBodyFont,
                    color = BloopDark,
                    modifier = Modifier
                      .padding(start = 12.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color.White)
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Button(
                modifier = Modifier
                  .padding(horizontal = 24.dp)
                  .fillMaxWidth()
                  .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloopTeal),
                onClick = { onItemClick(World) }
            ) {
                Text("Keep Building", fontFamily = BloopTitleFont)
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(55.dp)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .offset(y = 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BloopTeal)
                )
                Button(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors= ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BloopDark
                    ),
                    onClick={onItemClick(Draw)}
                ){
                    Text("Draw a Block", fontFamily = BloopTitleFont)
                }

            }
            Button(
                modifier = Modifier
                  .padding(horizontal = 24.dp)
                  .fillMaxWidth()
                  .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloopTeal),
                onClick = { onItemClick(Gallery) }
            ) {
                Text("My Blocks", fontFamily = BloopTitleFont)
            }


            // TODO: Extend app UI with more Jetpack Compose UI elements here

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name !",
        modifier = modifier.padding(16.dp, 16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    BloopyBlocksTheme { MainScreen(listOf("Android")) }
}

