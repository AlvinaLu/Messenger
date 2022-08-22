package com.android.example.messenger.data.db

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.ContactsModel
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val context: Context
) {

    val allCategories: Flow<List<CategoryModel>> = categoryDao.getCategoriesFlow()

    @WorkerThread
    suspend fun insertOrUpdate(name: String) {
        val currentCategory = categoryDao.getCategoriesByName(name)
        if (currentCategory != null) {

        } else {
            categoryDao.insertCategories(CategoryModel(name))
        }

    }

    @WorkerThread
    suspend fun delete(name: String) {
        val currentCategory = categoryDao.getCategoriesByName(name)
        if (currentCategory != null) {
            categoryDao.delete(currentCategory)
        }
    }

    @WorkerThread
    suspend fun clearAllData() {
        categoryDao.deleteAll()
    }


}