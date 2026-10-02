package com.chatflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatflow.app.data.Conversation
import com.chatflow.app.data.Message
import com.chatflow.app.data.Workflow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChatFlowApp() }
    }
}

@Composable
fun ChatFlowApp(vm: MainViewModel = viewModel()) {
    val state by vm.state
    MaterialTheme {
        when (val screen = state.screen) {
            Screen.Login -> AuthScreen(state.username, state.password, state.error, state.loading, false, vm)
            Screen.Register -> AuthScreen(state.username, state.password, state.error, state.loading, true, vm)
            Screen.Conversations -> ConversationScreen(state.username, state.conversations, state.error, vm)
            is Screen.Chat -> ChatScreen(screen, state, vm)
        }
    }
}

@Composable
private fun AuthScreen(username: String, password: String, error: String?, loading: Boolean, register: Boolean, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ChatFlow", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(if (register) "创建账号" else "登录", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(username, { vm.updateCredentials(it, password) }, label = { Text("用户名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { vm.updateCredentials(username, it) }, label = { Text("密码") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.login(register) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
            else Text(if (register) "注册并登录" else "登录")
        }
        TextButton(onClick = { if (register) vm.goLogin() else vm.goRegister() }) {
            Text(if (register) "已有账号？去登录" else "没有账号？去注册")
        }
    }
}

@Composable
private fun ConversationScreen(username: String, conversations: List<Conversation>, error: String?, vm: MainViewModel) {
    Scaffold(topBar = {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("会话", style = MaterialTheme.typography.headlineSmall); Text(username, style = MaterialTheme.typography.bodySmall) }
            Row {
                TextButton(onClick = vm::logout) { Text("退出") }
                Button(onClick = vm::createConversation) { Text("新建") }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
            if (conversations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无会话，点击右上角新建") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(conversations, key = { it.id }) { conversation ->
                        Card(onClick = { vm.openChat(conversation) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(conversation.title, style = MaterialTheme.typography.titleMedium)
                                Text("会话 #${conversation.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(screen: Screen.Chat, state: MainUiState, vm: MainViewModel) {
    var draft by remember { mutableStateOf("") }
    var showWorkflows by remember { mutableStateOf(false) }
    var workflowInput by remember { mutableStateOf("") }

    Scaffold(topBar = {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = vm::backToConversations) { Text("返回") }
            Column(Modifier.weight(1f)) {
                Text(screen.title, style = MaterialTheme.typography.titleLarge)
                Text(state.wsStatus, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { vm.loadWorkflows(); showWorkflows = true }) { Text("工作流") }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.messages, key = { it.id }) { message -> MessageBubble(message) }
            }
            state.activeRun?.let { run ->
                Card(Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("工作流：${run.status}", style = MaterialTheme.typography.titleSmall)
                        run.currentNodeId?.let { Text("当前节点：$it", style = MaterialTheme.typography.bodySmall) }
                        if (run.status == "WAITING") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(workflowInput, { workflowInput = it }, label = { Text("输入") }, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { vm.submitWorkflowInput(workflowInput); workflowInput = "" }) { Text("提交") }
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(draft, { draft = it }, label = { Text("输入消息") }, modifier = Modifier.weight(1f), maxLines = 4)
                Spacer(Modifier.width(8.dp))
                Button(enabled = !state.sending && draft.isNotBlank(), onClick = { vm.send(draft); draft = "" }) { Text("发送") }
            }
        }
    }
    if (showWorkflows) WorkflowDialog(state.workflows, { showWorkflows = false }, { vm.runWorkflow(it); showWorkflows = false })
}

@Composable
private fun MessageBubble(message: Message) {
    val mine = message.senderType == "USER"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Card(Modifier.fillMaxWidth(0.82f)) {
            Column(Modifier.padding(12.dp)) {
                Text(if (mine) "我" else "助手", style = MaterialTheme.typography.labelSmall)
                Text(message.content)
            }
        }
    }
}

@Composable
private fun WorkflowDialog(workflows: List<Workflow>, onDismiss: () -> Unit, onRun: (Long) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("选择工作流") }, text = {
        if (workflows.isEmpty()) Text("暂无可用工作流")
        else LazyColumn {
            items(workflows, key = { it.id }) { workflow ->
                TextButton(onClick = { onRun(workflow.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text(workflow.name)
                        workflow.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}
