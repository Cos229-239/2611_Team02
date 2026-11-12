package com.example.bloopyblocks.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloopyblocks.data.DataRepository
import com.example.bloopyblocks.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.launch
import java.util.UUID

class MainScreenViewModel(private val blockRepository: BlockRepository) : ViewModel() {
  val uiState: StateFlow<MainScreenUiState> =
    blockRepository.allBlocks
      .map<List<BlockEntity>, MainScreenUiState>(::Success)
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)
  fun insertTestBlock(){
    viewModelScope.launch{
      val block = BlockEntity(id=UUID.randomUUID().toString(),name="bloop",pixels=listOf(1,0,1,0))
      blockRepository.insertBlock(block)

    }
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<BlockEntity>) : MainScreenUiState
}
