package com.android.example.messenger.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
class ContactsModel(
    @PrimaryKey
    val id: Long = -1,
    val username: String = "",
    val phoneNumber: String = "",
    val status: String = "",
    val createdAt: String = "",
    val imgUrl: String = "",
    val lastOnline: String = "",
    var notificationToken: String = "",
)
