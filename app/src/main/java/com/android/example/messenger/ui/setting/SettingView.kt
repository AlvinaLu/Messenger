package com.android.example.messenger.ui.setting

import com.android.example.messenger.ui.auth.AuthView
import com.android.example.messenger.ui.base.BaseView
import java.io.File

interface SettingView : BaseView {

    fun showProgress()

    fun hideProgress()

    fun showSaveImgError()

    fun saveImgSuccess()

    fun saveImgUrl(url: String)

}