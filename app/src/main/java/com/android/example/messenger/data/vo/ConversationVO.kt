package com.android.example.messenger.data.vo

data class ConversationVO(
    val conversationId: Long,
    val secondPartyUsername: String,
    val imgUrl: String,
    val lastOnline: String,
    val messages: ArrayList<MessageVO>
)

