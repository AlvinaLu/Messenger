package com.android.example.messenger.data.remote.request

data class UserRequestObject(
    val username: String,
    val password: String,
    val phoneNumber: String = ""
)