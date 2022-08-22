package com.android.example.messenger.data.db

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.models.ConversationModel
import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val conversationsDao: ConversationDao,
    private val messageDao: MessageDao,
    private val appWebApi: AppWebApi,
    private val context: Context
) {
    val allConversations: Flow<List<ConversationModel>> = conversationsDao.getConversationFlow()
    private val preferences: AppPreferences = AppPreferences.create(context)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ConversationModel) {
        conversationsDao.insertConversation(contact)
    }

    @WorkerThread
    suspend fun getConversationByCategory(category: String): List<ConversationModel> {
        return conversationsDao.getConversationByCategory(category)
    }

    @WorkerThread
    suspend fun insert(vararg contact: ConversationModel) {
        conversationsDao.insertConversation(*contact)
    }

    @WorkerThread
    suspend fun clearAllData() {
        conversationsDao.deleteAll()
    }


    @WorkerThread
    suspend fun insertOrUpdate(conversation: ConversationModel) {

        val currentIds = conversationsDao.getConversations().map {
            it.conversationId
        }
        if (currentIds.contains(conversation.conversationId)) {
            conversationsDao.updateConversation(conversation)
        } else {
            conversationsDao.insertConversation(conversation)
        }
    }

    @WorkerThread
    suspend fun getConversationById(id: Long): ConversationModel? {
        val currentId = conversationsDao.getConversationById(id)
        return currentId
    }

    suspend fun fetchAll(): Boolean {
        try {
            val response = appWebApi.listConversations(preferences.accessToken as String).get()
            response
                ?.conversations
                ?.forEach {
                    val lastMessage = it.messages[it.messages.size - 1].body
                    val oldConversation = getConversationById(it.conversationId)
                    val conversation = ConversationModel(
                        conversationId = it.conversationId,
                        secondPartyUsername = it.secondPartyUsername,
                        imgUrl = it.imgUrl,
                        lastOnline = it.lastOnline,
                        unread = it.unread,
                        lastMessage = lastMessage,
                        lastMessageTime = it.messages[it.messages.size - 1].createdAt,
                        recipientId = it.secondPartyId,
                        pinned = oldConversation?.pinned == true,
                        category = oldConversation?.category
                    )

                    insertOrUpdate(conversation)
                }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}