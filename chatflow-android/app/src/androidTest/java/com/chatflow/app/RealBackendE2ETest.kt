package com.chatflow.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@RunWith(AndroidJUnit4::class)
class RealBackendE2ETest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private val http = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()
    private val gson = Gson()
    private val baseUrl = "http://10.0.2.2:8080"

    @Test
    fun fullUserChatWorkflowAgainstRealBackend() {
        val username = "android_e2e_" + System.currentTimeMillis()
        val password = "strong-pass-123"
        val token = register(username, password)

        composeRule.onNodeWithText("ChatFlow").assertIsDisplayed()
        composeRule.onNodeWithText("用户名").performClick().performTextInput(username)
        composeRule.onNodeWithText("密码").performClick().performTextInput(password)
        composeRule.onAllNodesWithText("登录").onLast().performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) { composeRule.onAllNodesWithText("会话").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("新建").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) { composeRule.onAllNodesWithText("输入消息").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("输入消息").performClick().performTextInput("real backend message")
        composeRule.onNodeWithText("发送").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) { composeRule.onAllNodesWithText("real backend message").fetchSemanticsNodes().isNotEmpty() }

        val conversationId = findLatestConversation(token)
        seedWaitingWorkflow(token)
        composeRule.onNodeWithText("工作流").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { composeRule.onAllNodesWithText("Android CI WAITING").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("Android CI WAITING").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) { composeRule.onAllNodesWithText("工作流：WAITING").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("输入").performClick().performTextInput("android-e2e-answer")
        composeRule.onNodeWithText("提交").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) { composeRule.onAllNodesWithText("工作流：SUCCESS").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("工作流：SUCCESS").assertIsDisplayed()

        val history = request("GET", "/api/v1/conversations/" + conversationId + "/messages", token = token)
        assertTrue(history.contains("real backend message"))
        assertTrue(history.contains("Android E2E answer"))
    }

    private fun register(username: String, password: String): String {
        val body = """{"username":"$username","password":"$password","nickname":"Android E2E"}"""
        val response = request("POST", "/api/v1/auth/register", body = body)
        return gson.fromJson(response, JsonObject::class.java)["accessToken"].asString
    }

    private fun findLatestConversation(token: String): Long {
        val response = request("GET", "/api/v1/conversations", token = token)
        val array = gson.fromJson(response, com.google.gson.JsonArray::class.java)
        return array.last().asJsonObject["id"].asLong
    }

    private fun seedWaitingWorkflow(token: String) {
        val definition = """{"startNodeId":"start","nodes":[{"id":"start","type":"START"},{"id":"wait","type":"WAIT_INPUT","variable":"answer"},{"id":"done","type":"SEND_MESSAGE","content":"Android E2E answer"},{"id":"end","type":"END"}],"edges":[{"from":"start","to":"wait"},{"from":"wait","to":"done"},{"from":"done","to":"end"}]}"""
        val body = """{"name":"Android CI WAITING","description":"Real backend emulator workflow","definition":$definition}"""
        request("POST", "/api/v1/workflows", body = body, token = token)
    }

    private fun request(method: String, path: String, body: String? = null, token: String? = null): String {
        val builder = Request.Builder().url(baseUrl + path).header("Accept", "application/json")
        token?.let { builder.header("Authorization", "Bearer " + it) }
        if (body != null) builder.method(method, body.toRequestBody(jsonMediaType)) else builder.method(method, null)
        http.newCall(builder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            check(response.isSuccessful) { method + " " + path + " failed: " + response.code + " " + text }
            return text
        }
    }
}