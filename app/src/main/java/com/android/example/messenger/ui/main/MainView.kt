package com.android.example.messenger.ui.main

import com.android.example.messenger.ui.base.BaseView

interface MainView : BaseView {

    fun showConversationsLoadError()

    fun showContactsLoadError()

    fun showConversationsScreen()

    fun showContactsScreen()

    fun getContactsFragment(): ContactsFragment

    fun getConversationsFragment(): ConversationsFragment

    fun showNoConversations()

    fun navigateToLogin()

    fun navigateToSettings()
    fun navigateToExperimental()
}
