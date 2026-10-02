package com.chatflow.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StompMessageParserTest {
    @Test
    fun parsesMessageBody() {
        val frame = "MESSAGE\nsubscription:sub-1\ndestination:/topic/conversations/7\n\n" +
            """{"id":7,"content":"hello"}""" + "\u0000"
        assertEquals("""{"id":7,"content":"hello"}""", StompMessageParser.messageBody(frame))
    }

    @Test
    fun identifiesControlFrames() {
        assertTrue(StompMessageParser.isConnected("CONNECTED\nversion:1.2\n\n\u0000"))
        assertTrue(StompMessageParser.isError("ERROR\nmessage:denied\n\n\u0000"))
        assertFalse(StompMessageParser.isConnected("MESSAGE\n\n{}\u0000"))
    }
}
