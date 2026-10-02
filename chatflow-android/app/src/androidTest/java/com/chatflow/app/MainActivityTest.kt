package com.chatflow.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginScreenIsDisplayedOnFreshInstall() {
        composeRule.onNodeWithText("ChatFlow").assertIsDisplayed()
        composeRule.onNodeWithText("登录").assertIsDisplayed()
        composeRule.onNodeWithText("用户名").assertIsDisplayed()
    }
}
