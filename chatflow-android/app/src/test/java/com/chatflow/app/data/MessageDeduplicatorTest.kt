package com.chatflow.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageDeduplicatorTest {
    @Test
    fun acceptsFirstMessageAndRejectsDuplicateId() {
        val dedupe = MessageDeduplicator()
        assertTrue(dedupe.accept(1L))
        assertFalse(dedupe.accept(1L))
        assertTrue(dedupe.accept(2L))
    }

    @Test
    fun seedPreventsHistoryThenWebSocketDuplicate() {
        val dedupe = MessageDeduplicator()
        dedupe.seed(listOf(10L, 11L))
        assertFalse(dedupe.accept(10L))
        assertTrue(dedupe.accept(12L))
    }
}
