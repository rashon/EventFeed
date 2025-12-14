package com.example.eventfeed.data.local

import com.example.eventfeed.domain.model.Event

fun EventEntity.toDomain(): Event =
    Event(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        createdAt = createdAt
    )

fun Event.toEntity(): EventEntity =
    EventEntity(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        createdAt = createdAt
    )