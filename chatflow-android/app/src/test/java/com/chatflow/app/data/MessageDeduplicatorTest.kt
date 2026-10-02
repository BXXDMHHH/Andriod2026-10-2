package com.chatflow.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageDeduplicatorTest {
    private fun message(id: Long) = Message(id, 1L, "USER", "m$id", "2026-01-01T00:00:00Z")

    @Test
    fun acceptsFirstMessageAndRejectsDuplicateId() {
        val dedupe = MessageDeduplicator()
        assertTrue(dedupe.accept(message(1)))
        assertFalse(dedupe.accept(message(1)))
        assertTrue(dedupe.accept(message(2)))
    }

    @Test
    fun seedPreventsHistoryThenWebSocketDuplicate() {
        val dedupe = MessageDeduplicator()
        dedupe.seed(listOf(message(10), message(11)))
        assertFalse(dedupe.accept(message(10)))
        assertTrue(dedupe.accept(message(12)))
    }
}
