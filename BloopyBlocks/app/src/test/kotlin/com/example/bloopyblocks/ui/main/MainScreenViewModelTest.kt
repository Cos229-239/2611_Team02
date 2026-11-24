package com.example.bloopyblocks.ui.main

import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_initiallyLoading() = runTest {
    val viewModel = MainScreenViewModel(FakeBlockRepository())
    assertEquals(viewModel.uiState.first(), MainScreenUiState.Loading)
  }

  @Test
  fun uiState_onItemSaved_isDisplayed() = runTest {
    val viewModel = MainScreenViewModel(FakeBlockRepository())
    assertEquals(viewModel.uiState.first(), MainScreenUiState.Loading)
  }
}

private class FakeBlockRepository : BlockRepository {
  override val allBlocks: Flow<List<BlockEntity>> = flow { emit(emptyList()) }
  override suspend fun insertBlock(block: BlockEntity) {}
  override suspend fun updateBlock(block: BlockEntity) {}
  override suspend fun deleteBlock(block: BlockEntity) {}
  override suspend fun getBlockById(id: String): BlockEntity? = null
}
