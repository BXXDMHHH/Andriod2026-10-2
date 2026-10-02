package com.chatflow.app.data

object StompMessageParser {
    fun messageBody(frame: String): String? {
        if (!frame.startsWith("MESSAGE")) return null
        return frame.substringAfter("\n\n", "").trimEnd('\u0000')
    }

    fun isConnected(frame: String): Boolean = frame.startsWith("CONNECTED")
    fun isError(frame: String): Boolean = frame.startsWith("ERROR")
}
