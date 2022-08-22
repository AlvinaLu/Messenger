package com.android.example.messenger.data.db

import androidx.room.*
import com.android.example.messenger.models.CategoryModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY categoryName ASC")
    fun getCategoriesFlow(): Flow<List<CategoryModel>>

    @Query("SELECT * FROM categories ORDER BY categoryName ASC")
    suspend fun getCategories(): List<CategoryModel>

    @Query("SELECT * FROM categories  WHERE categoryName = :name")
    fun getCategoriesByName(name: String): CategoryModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(name: CategoryModel)

    @Update
    suspend fun updateCategories(name: CategoryModel)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: CategoryModel)

}