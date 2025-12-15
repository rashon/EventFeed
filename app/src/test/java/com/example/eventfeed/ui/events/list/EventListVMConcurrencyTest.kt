package com.example.eventfeed.ui.events.list

import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class EventListVMConcurrencyTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class BlockingRepo : EventsRepository {
        private val blocker = CompletableDeferred<Unit>()
        val activeCalls = AtomicInteger(0)
        val maxActive = AtomicInteger(0)
        val requestedPages = mutableListOf<Int>()

        override fun cachedEventsFlow() = flowOf(emptyList<Event>())

        override suspend fun fetchAndCachePage(page: Int, pageSize: Int): List<Event> {
            requestedPages.add(page)
            val now = activeCalls.incrementAndGet()
            maxActive.updateAndGet { prev -> if (now > prev) now else prev }
            try {
                blocker.await() // block until test releases
            } finally {
                activeCalls.decrementAndGet()
            }
            return emptyList()
        }

        fun releaseAll() {
            if (!blocker.isCompleted) blocker.complete(Unit)
        }
    }

    @Test
    fun `concurrent refresh and loadMore are serialized (no overlap)`() = runTest {
        val repo = BlockingRepo()
        val vm = EventListVM(repo)

        // let init refresh / launched requests start
        runCurrent()

        // trigger an explicit refresh and then immediately loadMore
        vm.refresh()
        runCurrent()
        vm.loadMore()
        runCurrent()

        // if both overlapped, maxActive would be > 1
        assertEquals(1, repo.maxActive.get())

        // allow blocked requests to finish
        repo.releaseAll()
        advanceUntilIdle()

        // sanity: ensure at least one request happened
        assertTrue(repo.requestedPages.isNotEmpty())
    }

    @Test
    fun `concurrent multiple loadMore calls are serialized (no overlap)`() = runTest {
        val repo = BlockingRepo()
        val vm = EventListVM(repo)

        // let init refresh / launched requests start
        runCurrent()

        // call loadMore twice in quick succession
        vm.loadMore()
        vm.loadMore()
        runCurrent()

        // ensure fetches did not run concurrently
        assertEquals(1, repo.maxActive.get())

        // release blocked calls and finish
        repo.releaseAll()
        advanceUntilIdle()

        // at least one loadMore request should have been recorded
        assertTrue(repo.requestedPages.isNotEmpty())
    }
}