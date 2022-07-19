package com.android.example.messenger.data.vo

data class MessageVO(
    val id: Long,
    val senderId: Long,
    val recipientId: Long,
    val conversationId: Long,
    val body: String,
    var createdAt: String,
    var unread: Long
)

