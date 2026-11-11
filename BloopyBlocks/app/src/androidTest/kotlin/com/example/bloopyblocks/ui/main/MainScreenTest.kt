package com.example.bloopyblocks.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.bloopyblocks.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MainScreen(FAKE_DATA) }
  }

  @Test
  fun homeScreen_showsTitleAndButtons() {
    composeTestRule.onNodeWithText("My World").assertExists()
    composeTestRule.onNodeWithText("Keep Building").assertExists()
    composeTestRule.onNodeWithText("Draw a Block").assertExists()
    composeTestRule.onNodeWithText("My Blocks").assertExists()
  }
}

private val FAKE_DATA = listOf("Sample1", "Sample2", "Sample3")
