package com.android.example.messenger.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation")
class ConversationModel(
    @PrimaryKey
    val conversationId: Long,
    val secondPartyUsername: String,
    val recipientId: Long,
    val imgUrl: String,
    val lastOnline: String,
    val unread: Int,
    val lastMessage: String,
    val lastMessageTime: String,
    var pinned: Boolean,
    var category: String?
)