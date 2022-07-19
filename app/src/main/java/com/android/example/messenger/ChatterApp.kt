package com.android.example.messenger

import android.app.Application
import com.android.example.messenger.ui.experimental.AppWebApi
import com.android.example.messenger.ui.experimental.ExperimentalDatabase
import com.android.example.messenger.ui.experimental.ExperimentalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ChatterApp: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { ExperimentalDatabase.getDatabase(this, applicationScope) }
    val webApi by lazy { AppWebApi.getApiService()}
    val repositoryDao by lazy { ExperimentalRepository(database.contactsDao(), webApi, this) }

    override fun onCreate() {
        super.onCreate()
    }
}