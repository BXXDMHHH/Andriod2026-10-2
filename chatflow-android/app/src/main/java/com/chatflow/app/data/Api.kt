package com.chatflow.app.data

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val nickname: String? = null)
data class AuthResponse(val accessToken: String, val tokenType: String, val expiresIn: Long, val userId: Long, val username: String)
data class UserResponse(val id: Long, val username: String, val nickname: String?)
data class CreateConversationRequest(val title: String)
data class Conversation(val id: Long, val title: String, val createdAt: String, val updatedAt: String)
data class SendMessageRequest(val content: String, val clientMsgId: String? = null)
data class Message(val id: Long, val conversationId: Long, val senderType: String, val senderId: Long?, val contentType: String, val content: String, val clientMsgId: String?, val createdAt: String)
data class Workflow(val id: Long, val name: String, val description: String?, val version: Int, val status: String, val definition: JsonElement?, val createdAt: String, val updatedAt: String)
data class RunRequest(val conversationId: Long, val variables: Map<String, Any>? = null)
data class InputRequest(val input: Any)
data class NodeRun(val id: Long, val nodeId: String, val nodeType: String, val status: String, val input: JsonElement?, val output: JsonElement?, val startedAt: String?, val endedAt: String?, val errorMessage: String?)
data class WorkflowRun(val id: Long, val workflowId: Long, val conversationId: Long, val status: String, val currentNodeId: String?, val variables: JsonElement?, val startedAt: String?, val endedAt: String?, val errorMessage: String?, val nodes: List<NodeRun> = emptyList())

interface ChatFlowApi {
    @POST("api/v1/auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("api/v1/auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @GET("api/v1/auth/me") suspend fun me(): UserResponse
    @GET("api/v1/conversations") suspend fun conversations(): List<Conversation>
    @POST("api/v1/conversations") suspend fun createConversation(@Body request: CreateConversationRequest): Conversation
    @GET("api/v1/conversations/{id}/messages") suspend fun messages(@Path("id") conversationId: Long, @Query("limit") limit: Int = 50): List<Message>
    @POST("api/v1/conversations/{id}/messages") suspend fun sendMessage(@Path("id") conversationId: Long, @Body request: SendMessageRequest): Message
    @GET("api/v1/workflows") suspend fun workflows(): List<Workflow>
    @POST("api/v1/workflows/{id}/run") suspend fun runWorkflow(@Path("id") workflowId: Long, @Body request: RunRequest): WorkflowRun
    @GET("api/v1/workflow-runs/{id}") suspend fun workflowRun(@Path("id") runId: Long): WorkflowRun
    @POST("api/v1/workflow-runs/{id}/input") suspend fun submitInput(@Path("id") runId: Long, @Body request: InputRequest): WorkflowRun
}
