package com.android.example.messenger.data.response

data class ConversationVO(
    val conversationId: Long,
    val secondPartyUsername: String,
    val secondPartyId: Long,
    val imgUrl: String,
    val lastOnline: String,
    val messages: ArrayList<MessageVO>,
    val unread: Int
)

