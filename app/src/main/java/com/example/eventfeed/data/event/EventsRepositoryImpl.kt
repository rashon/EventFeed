package com.example.eventfeed.data.event

import com.example.eventfeed.data.local.AppDatabase
import com.example.eventfeed.data.local.toDomain
import com.example.eventfeed.data.local.toEntity
import com.example.eventfeed.data.remote.EventDto
import com.example.eventfeed.data.remote.EventsResponseDto
import com.example.eventfeed.domain.model.Event
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EventsRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val db: AppDatabase
) : EventsRepository {

    override suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event> =

        withContext(Dispatchers.IO) {
            try {

                val response: EventsResponseDto = client.get("$baseUrl/events") {
                    parameter("page", page)
                    parameter("size", pageSize)
                }.body()

                val dtos: List<EventDto> = response.events

                val events = dtos.map { it.toDomain() }

                cacheEvents(events)

                events
            } catch (e: Exception) {
                throw e
            }
        }

    private suspend fun cacheEvents(events: List<Event>) {
        db.eventDao().insertAll(events.map { it.toEntity() })
    }

    override fun cachedEventsFlow(): Flow<List<Event>> =
        db.eventDao().getAllEventsFlow().map { list -> list.map { it.toDomain() } }
}