package com.android.example.messenger.ui.setting

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File

interface SettingPresenter {

    fun sendUrl(url: String)

    fun getUrl()

}