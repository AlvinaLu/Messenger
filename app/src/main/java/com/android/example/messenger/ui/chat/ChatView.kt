package com.android.example.messenger.ui.chat

import com.android.example.messenger.data.vo.MessageVO
import com.android.example.messenger.ui.base.BaseView

interface ChatView : BaseView {

    interface ChatAdapter {
        fun navigateToChat(recipientName: String, recipientId: Long, mgUrl: String, lastOnline: String, conversationId: Long? = null )
    }

    fun showConversationLoadError()

    fun showMessageSendError()

    fun onLoadSuccess(secondPartyUsername: String, date: String, url: String, adapterMessages: AdapterMessages, conversationId: Long?)

    fun onSendSuccess(adapterMessages: AdapterMessages, messageVO: MessageVO)

    fun getRecipientName(): String?

    fun onLoadSuccessNewMessage(adapterMessages: AdapterMessages)
}