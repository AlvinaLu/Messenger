package com.android.example.messenger.ui.main

import com.android.example.messenger.data.vo.MessageVO

interface MainPresenter {

    fun loadConversations()

    fun loadContacts()

    fun executeLogout()

    fun addMessageReceive(conversationID: Long, messageVO: MessageVO)
}