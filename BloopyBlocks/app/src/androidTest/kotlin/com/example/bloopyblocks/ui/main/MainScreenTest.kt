package com.example.bloopyblocks.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.bloopyblocks.data.local.BlockEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.bloopyblocks.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()



  @Test
  fun homeScreen_showsTitleAndButtons() {
    // open Home with no saved blocks
    composeTestRule.setContent { MainScreen(emptyList()) }
    composeTestRule.onNodeWithText("My World").assertExists()
    composeTestRule.onNodeWithText("Keep Building").assertExists()
    composeTestRule.onNodeWithText("Draw a Block").assertExists()
    // "My Blocks" shows up twice, the big button and the bottom bar
    composeTestRule.onAllNodesWithText("My Blocks").assertCountEquals(2)
  }

  // with no saved blocks, Home asks the kid to draw their first one
  @Test
  fun homeScreen_noBlocks_showsDrawFirstBlock() {
    composeTestRule.setContent { MainScreen(emptyList()) }
    composeTestRule.onNodeWithText("Draw your first block!").assertExists()
  }

  // with 2 saved blocks, Home shows how many there are
  @Test
  fun homeScreen_twoBlocks_showsCount() {
    // two fake blocks, the pixels are empty since we only care about the count
    val blocks = listOf(
      BlockEntity("1", "Bird", List(64) { 0 }),
      BlockEntity("2", "Sun", List(64) { 0 })
    )
    composeTestRule.setContent { MainScreen(blocks) }
    composeTestRule.onNodeWithText("You have 2 blocks").assertExists()
  }
}

private val FAKE_DATA = listOf("Sample1", "Sample2", "Sample3")
