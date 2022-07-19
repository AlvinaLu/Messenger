package com.android.example.messenger.ui.main

import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.UserListVO
import com.android.example.messenger.data.vo.UserVO

interface MainInteractor {

    interface OnConversationsLoadFinishedListener {
        fun onConversationsLoadSuccess(conversationsListVo: ConversationListVO)

        fun onConversationsLoadError()
    }

    interface OnContactsLoadFinishedListener {
        fun onContactsLoadSuccess(userListVO: UserListVO)

        fun onContactsLoadError()
    }

    interface OnLogoutFinishedListener {
        fun onLogoutSuccess()
    }

    fun loadContacts(listener: MainInteractor.OnContactsLoadFinishedListener)

    fun loadConversations(listener: MainInteractor.OnConversationsLoadFinishedListener)

    fun logout(listener: MainInteractor.OnLogoutFinishedListener)
}