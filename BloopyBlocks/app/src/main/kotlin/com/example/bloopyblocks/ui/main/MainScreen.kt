package com.example.bloopyblocks.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.bloopyblocks.R
import com.example.bloopyblocks.data.DefaultDataRepository
import com.example.bloopyblocks.ui.theme.BloopyBlocksTheme
import com.example.bloopyblocks.data.RoomBlockRepository
import com.example.bloopyblocks.data.local.BlockDatabase
import com.example.bloopyblocks.data.local.BlockEntity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.bloopyblocks.data.remote.FireBaseAuthManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  var userId by remember {
    mutableStateOf<String?>(null)
  }
  var authError by remember {
    mutableStateOf<String?>(null)
  }
  LaunchedEffect(Unit) {
    try {
      userId=FireBaseAuthManager().getUserId()
    }
    catch(exception: Exception){
      authError=exception.message
    }
  }
  val viewModel: MainScreenViewModel = viewModel {
    val database = BlockDatabase.getDatabase(context)
    val repository = RoomBlockRepository(database.blockDao())
    MainScreenViewModel(repository)

  }
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
      MainScreen(data = state.data,onInsertClick={viewModel.insertTestBlock()}, onUpdateClick = {viewModel.updateTestBlock()}, onDeleteClick = {viewModel.deleteTestBlock()}, modifier = modifier)
    }
    is MainScreenUiState.Error -> {
      Text("Error loading data: ${state.throwable.message}")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(data: List<BlockEntity>, onInsertClick: ()->Unit, onUpdateClick: ()->Unit, onDeleteClick: ()->Unit, modifier: Modifier = Modifier) {
  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  ) { innerPadding ->

    // App Content

    Column(
      modifier = Modifier
        .padding(innerPadding)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      data.forEach { PrintBlock(it) }
      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        onClick = onInsertClick
      ) {
        Text("add block")
      }
      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        onClick = onUpdateClick
      ) {Text("update block")}
      Button(
        modifier = Modifier.padding(24.dp, 24.dp),
        onClick = onDeleteClick
      ){Text("delete block")}
      // TODO: Extend app UI with more Jetpack Compose UI elements here

    }
  }
}

@Composable
fun Greeting(s: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $s !",
    modifier = modifier.padding(16.dp, 16.dp),
    style = MaterialTheme.typography.bodyLarge
  )
}
@Composable
fun PrintBlock(block: BlockEntity, modifier: Modifier = Modifier) {
  val name = block.name
  val id = block.id
  Text(
    text = "block id: $id - block name: $name",
    modifier = modifier.padding(16.dp, 16.dp),
    style = MaterialTheme.typography.bodyLarge
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  BloopyBlocksTheme { MainScreen(listOf(BlockEntity(id = "1", name="testblock", pixels=listOf(1,0,1,0))), onInsertClick = {}, onUpdateClick = {}, onDeleteClick = {}) }
}

