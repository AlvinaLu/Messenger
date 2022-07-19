package com.android.example.messenger.ui.signup

import com.android.example.messenger.ui.auth.AuthView
import com.android.example.messenger.ui.base.BaseView

interface SignUpView : BaseView, AuthView {

    fun showProgress()

    fun showSignUpError()

    fun hideProgress()

    fun setUsernameError()

    fun setPhoneNumberError()

    fun setPasswordError()

    fun navigateToHome()

    fun setUsernameTooLongError()
}