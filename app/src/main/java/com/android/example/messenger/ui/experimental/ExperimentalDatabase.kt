package com.android.example.messenger.ui.experimental

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = arrayOf(ContactsModel::class), version = 1, exportSchema = false)
public abstract class ExperimentalDatabase : RoomDatabase(){

    abstract fun contactsDao(): ContactsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: ExperimentalDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): ExperimentalDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExperimentalDatabase::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }


}