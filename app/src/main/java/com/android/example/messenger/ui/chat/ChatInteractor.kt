package com.android.example.messenger.ui.chat

import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.ConversationVO
import com.android.example.messenger.data.vo.MessageVO

interface ChatInteractor {

    interface OnMessageSendFinishedListener {
        fun onSendSuccess(message: MessageVO)

        fun onSendError()
    }

    interface OnMessageLoadFinishedListener {
        fun onLoadSuccess(conversationVO: ConversationVO)

        fun onLoadError()
    }

    interface OnMessagesLoadFinishedListener {
        fun onLoadMessagesSuccess(conversationVO: ConversationListVO)

        fun onLoadMessagesError()
    }


    fun sendMessage(recipientId: Long, message: String, listener: OnMessageSendFinishedListener)

    fun loadMessages(conversationId: Long,  listener: OnMessageLoadFinishedListener)

    fun loadAllMessages(listener: OnMessagesLoadFinishedListener)

}