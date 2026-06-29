package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_resources")
data class HospitalResource(
    @PrimaryKey val id: String,
    val name: String,
    val currentValue: Int,
    val maxValue: Int,
    val unit: String,
    val category: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
