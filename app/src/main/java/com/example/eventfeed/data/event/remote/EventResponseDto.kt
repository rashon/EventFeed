package com.example.eventfeed.data.event.remote

import kotlinx.serialization.Serializable

@Serializable
data class EventsResponseDto(
    val page: Int = 0,
    val size: Int = 0,
    val total: Int = 0,
    val events: List<EventDto> = emptyList()
)