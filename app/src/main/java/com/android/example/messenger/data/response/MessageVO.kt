package com.android.example.messenger.data.response

import com.android.example.messenger.models.TYPE_OF_MESSAGE

data class MessageVO(
    val id: Long,
    val senderId: Long,
    val recipientId: Long,
    val conversationId: Long,
    val body: String,
    var createdAt: String,
    var unread: Long,
    var type: TYPE_OF_MESSAGE,
    var url: String,
    var latitude : String,
    var longitude: String

)

