package com.android.example.messenger.ui.chat

import com.android.example.messenger.data.vo.MessageVO
import com.example.messenger.utils.message.Message

interface ChatPresenter {

    fun sendMessage(senderId: Long, recipientId: Long, message: String)

    fun loadMessages(conversationId: Long)

    fun getTime(createdAt: String): String

    fun addMessageReceive(message: MessageVO?)

    fun deleteMessage(message: MessageVO?)


}