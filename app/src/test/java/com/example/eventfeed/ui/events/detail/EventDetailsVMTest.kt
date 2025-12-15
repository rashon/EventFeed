package com.example.eventfeed.ui.events.detail

import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class EventDetailsVMTest {

    private lateinit var mainThread: java.util.concurrent.ExecutorService

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Provide a single-threaded Main so viewModelScope launches run deterministically
        mainThread = Executors.newSingleThreadExecutor()
        Dispatchers.setMain(mainThread.asCoroutineDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThread.shutdownNow()
    }

    @Test
    fun `refresh success clears offline and error`() = runBlocking {
        var fetched = false

        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())

            override fun updateCurrentPage(page: Int) {currentPage = page }

            override var currentPage: Int
                get() = 0
                set(value) {}

            override suspend fun fetchAndCachePage(pageSize: Int): List<Event> {
                fetched = true
                return emptyList()
            }
        }

        val vm = EventDetailsVM(repo, eventId = "any")

        // trigger refresh and allow main tasks to run
        vm.refresh()
        withContext(Dispatchers.Main) { /* ensure refresh launch executed */ }

        // wait briefly for IO work to complete (fetch is immediate)
        Thread.sleep(50)

        assertTrue(fetched)
        assertFalse(vm.isOffline.value)
        assertNull(vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `refresh failure sets offline and error message`() = runBlocking {
        val repo = object : EventsRepository {
            override fun cachedEventsFlow(): Flow<List<Event>> = flowOf(emptyList())

            override fun updateCurrentPage(page: Int) {currentPage = page }

            override var currentPage: Int
                get() = 0
                set(value) {}
            override suspend fun fetchAndCachePage(pageSize: Int): List<Event> {
                throw RuntimeException("network failure")
            }
        }

        val vm = EventDetailsVM(repo, eventId = "any")

        vm.refresh()
        withContext(Dispatchers.Main) { /* ensure refresh launch executed */ }

        // wait briefly for IO work to run and complete
        Thread.sleep(50)

        assertTrue(vm.isOffline.value)
        assertEquals("network failure", vm.error.value)
        assertFalse(vm.isLoading.value)
    }
}