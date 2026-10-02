package com.chatflow.app.data

class MessageDeduplicator {
    private val ids = LinkedHashSet<Long>()

    fun accept(id: Long): Boolean = ids.add(id)

    fun seed(ids: Collection<Long>) {
        this.ids.addAll(ids)
    }

    fun clear() {
        ids.clear()
    }
}
