package com.android.example.messenger

import android.app.Application
import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ChatterApp: Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val webApi by lazy { AppWebApi.getApiService()}
    val contactsRepository by lazy { ContactsRepository(database.contactsDao(), webApi, this) }
    val conversationsRepository by lazy { ConversationRepository(database.conversationDao(), database.messageDao(),  webApi, this)}
    val messagesRepository by lazy { MessagesRepository(database.messageDao(), webApi, this) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao(), this) }

}