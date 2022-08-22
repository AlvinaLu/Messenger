package com.android.example.messenger.data.remote.request

import com.android.example.messenger.models.TYPE_OF_MESSAGE

data class MessageRequestObject(
    val recipientId: Long,
    val message: String,
    val type: TYPE_OF_MESSAGE,
    val url: String,
    val latitude: String,
    val longitude: String
)
