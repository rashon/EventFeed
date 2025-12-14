package com.example.eventfeed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)