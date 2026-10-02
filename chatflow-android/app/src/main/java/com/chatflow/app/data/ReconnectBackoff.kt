package com.chatflow.app.data

object ReconnectBackoff {
    fun delayMs(attempt: Int): Long {
        require(attempt >= 0)
        return (1000L shl attempt.coerceAtMost(4)).coerceAtMost(15000L)
    }
}
