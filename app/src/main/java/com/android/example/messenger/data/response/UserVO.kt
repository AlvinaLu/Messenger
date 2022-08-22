package com.android.example.messenger.data.response

data class UserVO(
    val id: Long,
    val username: String,
    val phoneNumber: String,
    val status: String,
    val createdAt: String,
    val imgUrl: String,
    val lastOnline: String,
    var notificationToken: String,
)