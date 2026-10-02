package com.chatflow.app

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chatflow.app.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.WebSocket

sealed interface Screen {
    data object Login : Screen
    data object Register : Screen
    data object Conversations : Screen
    data class Chat(val conversationId: Long, val title: String) : Screen
}
data class MainUiState(
    val screen: Screen = Screen.Login,
    val loading: Boolean = false,
    val error: String? = null,
    val username: String = "",
    val password: String = "",
    val conversations: List<Conversation> = emptyList(),
    val messages: List<Message> = emptyList(),
    val workflows: List<Workflow> = emptyList(),
    val activeRun: WorkflowRun? = null,
    val wsStatus: String = "未连接",
    val sending: Boolean = false
)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SessionStore(app)
    private val client = ChatFlowClient(app) { logout() }
    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private val messageDeduplicator = MessageDeduplicator()
    var state = mutableStateOf(MainUiState())
        private set

    init {
        if (store.token != null) {
            state.value = state.value.copy(screen = Screen.Conversations, username = store.username ?: "")
            loadConversations()
        }
    }
    fun updateCredentials(username: String, password: String) { state.value = state.value.copy(username = username, password = password, error = null) }
    fun goRegister() { state.value = state.value.copy(screen = Screen.Register, password = "", error = null) }
    fun goLogin() { state.value = state.value.copy(screen = Screen.Login, password = "", error = null) }
    fun backToConversations() {
        reconnectJob?.cancel()
        reconnectJob = null
        socket?.close(1000, "back")
        socket = null
        state.value = state.value.copy(screen = Screen.Conversations, messages = emptyList(), activeRun = null, error = null, wsStatus = "未连接")
        loadConversations()
    }
    fun login(register: Boolean = false) {
        val s = state.value
        if (s.username.isBlank() || s.password.isBlank()) { state.value = s.copy(error = "请输入用户名和密码"); return }
        state.value = s.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (register) client.api.register(RegisterRequest(s.username.trim(), s.password, s.username.trim()))
                else client.api.login(LoginRequest(s.username.trim(), s.password))
            }.onSuccess {
                store.token = it.accessToken
                store.username = it.username
                state.value = state.value.copy(loading = false, password = "", screen = Screen.Conversations)
                loadConversations()
            }.onFailure { state.value = state.value.copy(loading = false, error = it.message ?: "请求失败") }
        }
    }
    fun loadConversations() {
        viewModelScope.launch {
            runCatching { client.api.conversations() }
                .onSuccess { state.value = state.value.copy(conversations = it, loading = false, error = null) }
                .onFailure { state.value = state.value.copy(error = it.message ?: "加载会话失败", loading = false) }
        }
    }
    fun createConversation() {
        viewModelScope.launch {
            runCatching { client.api.createConversation(CreateConversationRequest("新会话")) }
                .onSuccess { state.value = state.value.copy(conversations = listOf(it) + state.value.conversations); openChat(it) }
                .onFailure { state.value = state.value.copy(error = it.message ?: "创建会话失败") }
        }
    }
    fun openChat(conversation: Conversation) = openChat(conversation.id, conversation.title, true)
    fun openChat(id: Long, title: String, resetReconnect: Boolean = true) {
        messageDeduplicator.clear()
        state.value = state.value.copy(screen = Screen.Chat(id, title), messages = emptyList(), activeRun = null, error = null, wsStatus = "连接中")
        viewModelScope.launch {
            runCatching { client.api.messages(id) }
                .onSuccess { messages ->
                    val sorted = messages.sortedBy { msg -> msg.createdAt }
                    messageDeduplicator.seed(sorted.map { it.id })
                    state.value = state.value.copy(messages = sorted)
                }
                .onFailure { state.value = state.value.copy(error = it.message ?: "历史消息加载失败") }
        }
        if (resetReconnect) {
            reconnectJob?.cancel()
            reconnectAttempt = 0
        }
        socket?.close(1000, "switch conversation")
        socket = client.connect(id, { message ->
            if (state.value.screen is Screen.Chat && (state.value.screen as Screen.Chat).conversationId == id &&
                messageDeduplicator.accept(message.id)) {
                state.value = state.value.copy(messages = state.value.messages + message)
            }
        }, { status ->
            state.value = state.value.copy(wsStatus = status)
            if (status.startsWith("连接失败")) scheduleReconnect(id, title)
        })
    }
    private fun scheduleReconnect(id: Long, title: String) {
        if (state.value.screen !is Screen.Chat || (state.value.screen as Screen.Chat).conversationId != id || store.token == null) return
        reconnectJob?.cancel()
        val delayMs = ReconnectBackoff.delayMs(reconnectAttempt)
        reconnectAttempt = (reconnectAttempt + 1).coerceAtMost(5)
        reconnectJob = viewModelScope.launch {
            state.value = state.value.copy(wsStatus = "将在 ${delayMs / 1000} 秒后重连")
            delay(delayMs)
            if (state.value.screen is Screen.Chat) openChat(id, title, false)
        }
    }

    fun send(text: String) {
        val screen = state.value.screen as? Screen.Chat ?: return
        if (text.isBlank()) return
        state.value = state.value.copy(sending = true, error = null)
        viewModelScope.launch {
            runCatching { client.api.sendMessage(screen.conversationId, SendMessageRequest(text, client.clientMessageId())) }
                .onSuccess { sent ->
                    if (messageDeduplicator.accept(sent.id)) state.value = state.value.copy(messages = state.value.messages + sent)
                }
                .onFailure { state.value = state.value.copy(error = it.message ?: "发送失败") }
                .also { state.value = state.value.copy(sending = false) }
        }
    }
    fun loadWorkflows() {
        viewModelScope.launch {
            runCatching { client.api.workflows() }
                .onSuccess { state.value = state.value.copy(workflows = it, error = null) }
                .onFailure { state.value = state.value.copy(error = it.message ?: "工作流加载失败") }
        }
    }
    fun runWorkflow(workflowId: Long) {
        val screen = state.value.screen as? Screen.Chat ?: return
        viewModelScope.launch {
            runCatching { client.api.runWorkflow(workflowId, RunRequest(screen.conversationId)) }
                .onSuccess { state.value = state.value.copy(activeRun = it, error = null) }
                .onFailure { state.value = state.value.copy(error = it.message ?: "工作流启动失败") }
        }
    }
    fun submitWorkflowInput(input: String) {
        val run = state.value.activeRun ?: return
        if (input.isBlank()) return
        viewModelScope.launch {
            runCatching { client.api.submitInput(run.id, InputRequest(input)) }
                .onSuccess { state.value = state.value.copy(activeRun = it, error = null) }
                .onFailure { state.value = state.value.copy(error = it.message ?: "工作流输入失败") }
        }
    }
    fun logout() {
        reconnectJob?.cancel()
        reconnectJob = null
        socket?.close(1000, "logout"); socket = null; store.clear(); state.value = MainUiState()
    }
    override fun onCleared() { reconnectJob?.cancel(); socket?.close(1000, "viewmodel cleared"); super.onCleared() }
}
