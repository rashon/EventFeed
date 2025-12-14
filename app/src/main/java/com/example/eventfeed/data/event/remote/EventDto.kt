package com.example.eventfeed.data.event.remote

import com.example.eventfeed.domain.model.Event
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class EventDto(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val start: String,
    val end: String? = null,
    val location: String? = null
) {
    fun toDomain(): Event {
        val idLong = id ?: 0L
        val startMillis = parseIsoToEpochMillis(start)
        val endMillis = end?.let { parseIsoToEpochMillis(it) }
        return Event(
            id = idLong,
            title = title,
            description = description,
            startTime = startMillis,
            endTime = endMillis,
            location = location,
            isAllDay = false,
            createdAt = System.currentTimeMillis()
        )
    }
}

private fun parseIsoToEpochMillis(iso: String): Long =
    try {
        Instant.parse(iso).toEpochMilli()
    } catch (e: Exception) {
        0L
    }