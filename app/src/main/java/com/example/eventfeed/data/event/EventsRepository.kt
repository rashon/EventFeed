package com.example.eventfeed.data.event

import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventsRepository {
    suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event>
    fun cachedEventsFlow(): Flow<List<Event>>
}