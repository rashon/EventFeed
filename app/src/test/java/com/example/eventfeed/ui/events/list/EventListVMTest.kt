package com.example.eventfeed.ui.events.list

import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EventListVMTest {

    private val testDispatcher = StandardTestDispatcher()

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

            override fun updateCurrentPage(page: Int) {
                currentPage = page
            }

            override suspend fun fetchAndCachePage(pageSize: Int): List<Event> {
                return emptyList()
            }

            override var currentPage: Int
                get() = 0
                set(value) {}
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

            override fun updateCurrentPage(page: Int) {
                currentPage = page
            }

            override var currentPage: Int
                get() = 0
                set(value) {}

            override suspend fun fetchAndCachePage(pageSize: Int): List<Event> {
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
    fun `loadMore stops after empty page (respects last page)`() =
        runTest(testDispatcher) { // Pass testDispatcher
        val requestedPages = mutableListOf<Int>()

        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())

            override fun updateCurrentPage(page: Int) {
                currentPage = page
            }

            override var currentPage: Int = 0

            override suspend fun fetchAndCachePage(pageSize: Int): List<Event> {
                if (currentPage > 0) {
                    requestedPages.add(currentPage)
                }
                return emptyList()
            }
        }

        val vm = EventListVM(repo)

        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(emptyList<Event>(), requestedPages)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        }
}