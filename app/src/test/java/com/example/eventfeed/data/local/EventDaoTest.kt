package com.example.eventfeed.data.local

import com.example.eventfeed.data.event.EventsRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventDaoTest {

    @Test
    fun cachedEventsFlow_emptyDaoEmitsEmptyList() = runTest {
        // FIX: Use MockEngine instead of mockk<HttpClient>
        val mockEngine = MockEngine {
            respond("[]", HttpStatusCode.OK)
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }

        val db = mockk<AppDatabase>()
        val dao = mockk<EventDao>()

        coEvery { db.eventDao() } returns dao
        coEvery { dao.getAllEventsFlow() } returns flowOf(emptyList())

        val repo = EventsRepositoryImpl(client = client, baseUrl = "https://api", db = db)

        val emitted = repo.cachedEventsFlow().first()
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun fetchAndCachePage_onHttpError_propagatesAndDoesNotInsert() = runTest {
        // FIX: Use MockEngine to simulate the exception
        val mockEngine = MockEngine {
            throw RuntimeException("network failure")
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }

        val db = mockk<AppDatabase>()
        val dao = mockk<EventDao>(relaxed = true)

        coEvery { db.eventDao() } returns dao
        // REMOVE THIS: coEvery { client.get(...) } is not needed because MockEngine handles it

        val repo = EventsRepositoryImpl(client = client, baseUrl = "https://api", db = db)

        try {
            repo.fetchAndCachePage(pageSize = 20)
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("network failure", e.message)
        }

        // ensure no insertion attempted when fetch failed
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }
}