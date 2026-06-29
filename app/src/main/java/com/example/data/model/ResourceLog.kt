package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resource_logs")
data class ResourceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val resourceId: String,
    val resourceName: String,
    val changeAmount: Int,
    val newValue: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String
)
