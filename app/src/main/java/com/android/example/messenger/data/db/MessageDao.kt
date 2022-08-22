package com.android.example.messenger.data.db

import androidx.room.*
import com.android.example.messenger.models.ConversationModel
import com.android.example.messenger.models.MessageModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE conversationId = :id ORDER BY createdAt ASC")
    fun getMessagesFlowByConversationId(id: Long): Flow<List<MessageModel>>

    @Query("SELECT * FROM message WHERE recipientId = :id or senderId = :id ORDER BY createdAt ASC")
    fun getMessagesFlowByRecipientId(id: Long): Flow<List<MessageModel>>

    @Query("SELECT * FROM message ORDER BY createdAt ASC")
    fun getMessagesFlow(): Flow<List<MessageModel>>

    @Query("SELECT * FROM message ORDER BY createdAt ASC")
    suspend fun getMessages(): List<MessageModel>

    @Query("SELECT * FROM message WHERE conversationId = :id ORDER BY createdAt ASC ")
    suspend fun getMessagesByConversationId(id: Long): List<MessageModel>

    @Query("SELECT * FROM message  WHERE id = :id")
    fun getMessageId(id: Long): MessageModel?

    @Query("SELECT * FROM message  WHERE localId = :localId")
    fun getMessageByLocalId(localId: String): MessageModel?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(vararg message: MessageModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageModel)

    @Update
    suspend fun updateMessage(vararg message: MessageModel)

    @Query("DELETE FROM message")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: MessageModel)
}