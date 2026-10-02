package com.chatflow.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class ChatFlowClient(context: Context) {
    private val store = SessionStore(context)
    private val gson: Gson = GsonBuilder().create()
    private val http = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder().apply {
            store.token?.let { header("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
    }.build()

    val api: ChatFlowApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ChatFlowApi::class.java)

    fun connect(conversationId: Long, onMessage: (Message) -> Unit, onStatus: (String) -> Unit): WebSocket? {
        val token = store.token ?: return null
        val request = Request.Builder().url(BuildConfig.WS_URL).build()
        return http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                onStatus("连接中")
                webSocket.send("CONNECT\\naccept-version:1.2\\nAuthorization:Bearer $token\\nheart-beat:10000,10000\\n\\n\\u0000")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                when {
                    StompMessageParser.isConnected(text) -> {
                        onStatus("已连接")
                        webSocket.send("SUBSCRIBE\\nid:sub-$conversationId\\ndestination:/topic/conversations/$conversationId\\nack:auto\\n\\n\\u0000")
                    }
                    StompMessageParser.messageBody(text) != null -> {
                        val body = text.substringAfter("\\n\\n", "").trimEnd('\\u0000')
                        runCatching { gson.fromJson(body, Message::class.java) }.getOrNull()?.let(onMessage)
                    }
                    StompMessageParser.isError(text) -> onStatus("WebSocket 鉴权/订阅失败")
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                onStatus("连接失败: " + (t.message ?: "未知错误"))
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { onStatus("已断开") }
        })
    }
    fun close(webSocket: WebSocket?) { webSocket?.close(1000, "screen closed") }
    fun clientMessageId(): String = UUID.randomUUID().toString()
}