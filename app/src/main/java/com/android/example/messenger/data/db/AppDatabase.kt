package com.android.example.messenger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.ContactsModel
import com.android.example.messenger.models.ConversationModel
import com.android.example.messenger.models.MessageModel
import kotlinx.coroutines.CoroutineScope

@Database(entities = [ContactsModel::class, ConversationModel::class, MessageModel::class, CategoryModel::class],  version = 13, exportSchema = false)
public abstract class AppDatabase : RoomDatabase(){

    abstract fun contactsDao(): ContactsDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }


}