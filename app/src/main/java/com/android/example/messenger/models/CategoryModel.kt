package com.android.example.messenger.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
class CategoryModel(
    @PrimaryKey
    val categoryName: String
)