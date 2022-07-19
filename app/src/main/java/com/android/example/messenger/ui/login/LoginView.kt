package com.android.example.messenger.ui.login

import com.android.example.messenger.ui.auth.AuthView
import com.android.example.messenger.ui.base.BaseView

interface LoginView : BaseView, AuthView {
    fun showProgress()

    fun hideProgress()

    fun setUsernameError()

    fun setPasswordError()

    fun navigateToSignUp()

    fun navigateToHome()
}