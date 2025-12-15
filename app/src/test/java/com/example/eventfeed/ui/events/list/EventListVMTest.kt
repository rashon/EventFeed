package com.example.eventfeed.ui.events.list

import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EventListVMTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial empty cache yields empty events list`() = runTest {
        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())
            override suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event> {
                return emptyList()
            }
        }

        val vm = EventListVM(repo)

        // allow init refresh/collectors to run
        advanceUntilIdle()

        assertTrue(vm.events.value.isEmpty())
        assertFalse(vm.isOffline.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `refresh failure sets offline and error`() = runTest {
        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())
            override suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event> {
                throw RuntimeException("network failure")
            }
        }

        val vm = EventListVM(repo)

        // let init/refresh run
        advanceUntilIdle()

        assertTrue(vm.isOffline.value)
        assertEquals("network failure", vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadMore stops after empty page (respects last page)`() = runTest {
        val requestedPages = mutableListOf<Int>()

        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())
            override suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event> {
                requestedPages.add(page)
                return emptyList() // simulate server returned empty -> last page
            }
        }

        val vm = EventListVM(repo)

        // allow initial refresh to run (will request page 0)
        advanceUntilIdle()

        // clear initial request record to focus on loadMore behavior
        requestedPages.clear()

        // call loadMore twice; second call should be no-op because first returned empty
        vm.loadMore()
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        // only one loadMore request for page 1 should have been made
        assertEquals(listOf(1), requestedPages)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }
}