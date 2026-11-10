package com.example.bloopyblocks.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.bloopyblocks.ui.theme.BloopCream
import com.example.bloopyblocks.ui.theme.BloopDark
import com.example.bloopyblocks.ui.theme.BloopTeal
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
      MainScreen(data = state.data, modifier = modifier ,onItemClick = onItemClick)
    }
    is MainScreenUiState.Error -> {
      Text("Error loading data: ${state.throwable.message}")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(data: List<String>, modifier: Modifier = Modifier ,onItemClick: (NavKey) -> Unit = {}) {
  Scaffold(
    modifier = modifier,
    containerColor = BloopCream,
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = BloopCream,
          titleContentColor = BloopDark
        )
      )
    }
  ) { innerPadding ->

    // App Content

    Column(
      modifier = Modifier.padding(innerPadding),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "My World",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = BloopDark,
        modifier = Modifier.padding(24.dp, 16.dp)
      )

      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloopTeal),
        onClick = { onItemClick(Draw) }
      ) {
        Text("Draw")
      }
      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloopTeal),
        onClick = { onItemClick(Gallery) }
      ) {
        Text("My Blocks")
      }
      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloopTeal),
        onClick = { onItemClick(World) }
      ) {
        Text("World")
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

