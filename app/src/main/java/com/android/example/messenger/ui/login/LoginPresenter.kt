package com.android.example.messenger.ui.login

interface LoginPresenter {
    fun executeLogin(username: String, password: String)

    fun putNotificationToken(token: String?)
}