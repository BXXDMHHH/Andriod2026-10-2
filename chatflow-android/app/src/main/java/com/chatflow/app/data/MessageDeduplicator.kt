package com.chatflow.app.data

class MessageDeduplicator {
    private val ids = LinkedHashSet<Long>()

    fun accept(message: Message): Boolean {
        if (!ids.add(message.id)) return false
        return true
    }

    fun seed(messages: Collection<Message>) {
        messages.forEach { ids.add(it.id) }
    }

    fun clear() {
        ids.clear()
    }
}
