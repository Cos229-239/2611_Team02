package com.example.bloopyblocks.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.bloopyblocks.ui.welcome.WelcomeScreen
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_showsTitleAndButton() {
        composeTestRule.setContent {
            WelcomeScreen(onStart = {})
        }
        composeTestRule.onNodeWithText("Bloopy Blocks").assertExists()
        composeTestRule.onNodeWithText("Start Building").assertExists()
    }

    @Test
    fun welcomeScreen_startButtonCallsOnStart() {
        var clicked = false
        composeTestRule.setContent {
            WelcomeScreen(onStart = { clicked = true })
        }
        composeTestRule.onNodeWithText("Start Building").performClick()
        assertTrue(clicked)
    }
}