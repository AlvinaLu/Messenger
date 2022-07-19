package com.example.messenger.utils.message

import java.util.*


data class Message(val senderId: Long, val recipientId: Long, val body: String, var createdAt: String) {
}