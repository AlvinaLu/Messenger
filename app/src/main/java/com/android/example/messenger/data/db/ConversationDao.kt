package com.android.example.messenger.data.db

import androidx.room.*
import com.android.example.messenger.models.ConversationModel
import kotlinx.coroutines.flow.Flow
@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation ORDER BY lastMessageTime DESC")
    fun getConversationFlow(): Flow<List<ConversationModel>>

    @Query("SELECT * FROM conversation ORDER BY lastMessageTime DESC")
    suspend fun getConversations(): List<ConversationModel>

    @Query("SELECT * FROM conversation  WHERE conversationId = :id")
    fun getConversationById(id: Long): ConversationModel?

    @Query("SELECT * FROM conversation  WHERE category = :category")
    fun getConversationByCategory(category: String): List<ConversationModel>

//    @Transaction
//    @Query("SELECT * FROM conversation")
//    fun getConversationWithMessages(): List<ConversationWithMessages>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(vararg contact: ConversationModel)

    @Update
    suspend fun updateConversation(vararg contact: ConversationModel)

    @Query("DELETE FROM conversation")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: ConversationModel)
}