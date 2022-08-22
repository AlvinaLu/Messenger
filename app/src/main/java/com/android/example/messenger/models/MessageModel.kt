package com.android.example.messenger.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageModel(
    @PrimaryKey
    val id: Long,
    val senderId: Long,
    val recipientId: Long,
    val conversationId: Long,
    var body: String,
    var createdAt: String,
    var unread: Long,
    var type: TYPE_OF_MESSAGE,
    var url: String,
    var uri: String?,
    var localId: String,
    var latitude: String,
    var longitude: String
)

enum class TYPE_OF_MESSAGE{TEXT, DOCUMENT, IMAGE, LOCATION}
