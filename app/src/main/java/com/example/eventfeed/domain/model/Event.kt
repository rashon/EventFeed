package com.example.eventfeed.domain.model

data class Event(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)