package com.android.example.messenger.data.vo

data class Conversation(
    val conversationId: Long,
    val secondPartyUsername: String,
    val imgUrl: String,
    val lastOnline: String,
    val messages: ArrayList<MessageVO>,
)