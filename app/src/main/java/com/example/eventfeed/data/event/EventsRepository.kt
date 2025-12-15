package com.example.eventfeed.data.event

import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventsRepository {
    suspend fun fetchAndCachePage(pageSize: Int): List<Event>
    fun cachedEventsFlow(): Flow<List<Event>>
    fun updateCurrentPage(page: Int)
    var currentPage: Int
}