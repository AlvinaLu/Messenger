package com.android.example.messenger.ui.base

import android.content.Context

interface BaseView {
    fun bindViews()

    fun getContext(): Context
}