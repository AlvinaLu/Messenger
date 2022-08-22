package com.android.example.messenger.data.db

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.MessageRequestObject
import com.android.example.messenger.data.remote.request.MessageReadRequest
import com.android.example.messenger.models.MessageModel
import kotlinx.coroutines.flow.Flow
import java.util.*

class MessagesRepository(
    private val messageDao: MessageDao,
    private val appWebApi: AppWebApi,
    private val context: Context,
) {
    val allMessages: Flow<List<MessageModel>> = messageDao.getMessagesFlow()
    private val preferences: AppPreferences = AppPreferences.create(context)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(vararg message: MessageModel) {
        messageDao.insertMessage(*message)
    }


    @WorkerThread
    suspend fun deleteByLocalId(message: MessageModel) {
        messageDao.delete(message)
    }


    @WorkerThread
    suspend fun getMessageBYLocalId(id: String): MessageModel? {
        return messageDao.getMessageByLocalId(id)
    }


    @WorkerThread
    suspend fun insertOrUpdate(message: MessageModel) {
        val currentIds = messageDao.getMessages().map {
            it.id
        }
        if (currentIds.contains(message.id)) {
            messageDao.updateMessage(message)
        } else {
            messageDao.insertMessage(message)
        }
    }

    @WorkerThread
    suspend fun insertOrUpdate(vararg message: MessageModel) {
        val currentIds = messageDao.getMessages().map {
            it.id
        }
        message.forEach {
            if (currentIds.contains(it.id)) {
                messageDao.updateMessage(it)
            } else {
                messageDao.insertMessage(it)
            }
        }
    }

    @WorkerThread
    fun getByConversationOrRecipientId(
        conversationId: Long,
        recipientId: Long
    ): Flow<List<MessageModel>> {
        return if (conversationId != -1L) {
            messageDao.getMessagesFlowByConversationId(conversationId)
        } else {
            messageDao.getMessagesFlowByRecipientId(recipientId)
        }
    }

    suspend fun readMessage(message: MessageModel) {
        try {
            appWebApi.readMessage(
                MessageReadRequest(preferences.userDetails.id, message.id),
                preferences.accessToken as String
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchConversationFromWebById(id: Long): Boolean {
        val messages = messageDao.getMessagesByConversationId(id)
        val lastMessage = messages.lastOrNull()
        var lastMessageId: Long = 0L
        if (lastMessage != null) {
            lastMessageId = lastMessage.id
        }
        try {
            val response =
                appWebApi.showConversation(id, lastMessageId, preferences.accessToken as String)
                    .get()

            val messages = response?.messages?.map {
                MessageModel(
                    id = it.id,
                    senderId = it.senderId,
                    recipientId = it.recipientId,
                    conversationId = it.conversationId,
                    body = it.body,
                    createdAt = it.createdAt,
                    unread = it.unread,
                    type = it.type,
                    url = it.url,
                    uri = null,
                    UUID.randomUUID().toString(),
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
            messages?.toTypedArray()?.let { insertOrUpdate(*it) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    @WorkerThread
    suspend fun clearAllData() {
        messageDao.deleteAll()
    }

    suspend fun sendMessage(
        message: MessageModel
    ): Boolean {
        try {
            val response = appWebApi.createMessage(
                MessageRequestObject(
                    message.recipientId,
                    message.body,
                    message.type,
                    message.url,
                    message.latitude,
                    message.longitude
                ),
                preferences.accessToken as String
            ).get()

            val messageReceive =
                MessageModel(
                    id = response.id,
                    senderId = response.senderId,
                    recipientId = response.recipientId,
                    conversationId = response.conversationId,
                    body = response.body,
                    createdAt = response.createdAt,
                    unread = response.unread,
                    type = response.type,
                    url = response.url,
                    uri = null,
                    localId = message.localId,
                    latitude = response.latitude,
                    longitude = response.longitude
                )

            val oldMessage = getMessageBYLocalId(message.localId)
            if (oldMessage != null) {
                deleteByLocalId(oldMessage)
            }
            insert(messageReceive)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}
