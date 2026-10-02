package com.chatflow.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ReconnectBackoffTest {
    @Test
    fun exponentialBackoffCapsAtFifteenSeconds() {
        assertEquals(1000L, ReconnectBackoff.delayMs(0))
        assertEquals(2000L, ReconnectBackoff.delayMs(1))
        assertEquals(4000L, ReconnectBackoff.delayMs(2))
        assertEquals(8000L, ReconnectBackoff.delayMs(3))
        assertEquals(15000L, ReconnectBackoff.delayMs(4))
        assertEquals(15000L, ReconnectBackoff.delayMs(8))
    }
}
