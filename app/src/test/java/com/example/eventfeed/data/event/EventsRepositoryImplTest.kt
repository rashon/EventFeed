package com.example.eventfeed.data.event

import com.example.eventfeed.data.local.AppDatabase
import com.example.eventfeed.data.local.EventDao
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
class EventsRepositoryImplTest {

    @Test
    fun cachedEventsFlow_emptyDaoEmitsEmptyList() = runTest {
        // 1. Setup a "real" client with a MockEngine that responds with empty JSON (or whatever is needed)
        // Since this test only checks the DB flow, the client might not even be called,
        // but we provide a dummy one to satisfy the constructor.
        val mockEngine = MockEngine { _ ->
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
        // 1. Setup MockEngine to THROW an exception when called
        val mockEngine = MockEngine { _ ->
            throw RuntimeException("network failure")
        }

        // 2. Create the real HttpClient using that engine
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }

        val db = mockk<AppDatabase>()
        val dao = mockk<EventDao>(relaxed = true)

        coEvery { db.eventDao() } returns dao

        val repo = EventsRepositoryImpl(client = client, baseUrl = "https://api", db = db)

        try {
            // 3. This triggers the client, which triggers the MockEngine, which throws the error
            repo.fetchAndCachePage(pageSize = 20)
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            // 4. Verify the error message matches what we threw in MockEngine
            assertEquals("network failure", e.message)
        }

        // 5. Verify DB was never touched
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }
}