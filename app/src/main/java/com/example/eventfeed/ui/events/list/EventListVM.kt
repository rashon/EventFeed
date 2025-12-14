package com.example.eventfeed.ui.events.list

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.domain.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class EventListVM(
    private val repo: EventsRepository
) : ViewModel() {

    private val pageSize: Int = 20
    private val refreshIntervalMs: Long = 3_000L

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPage = 0
    private var isLastPageReached = false
    private var inFlight = false

    // prevent overlapping refresh calls
    private val refreshMutex = Mutex()

    init {
        // observe cached events so UI updates when DB changes (offline support)
        viewModelScope.launch {
            repo.cachedEventsFlow().collect { list -> _events.value = list }
        }

        // initial load
        refresh()

        // periodic auto-refresh every 5 seconds (background)
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {

                delay(refreshIntervalMs)
                // attempt auto-refresh in background without forcing UI "refreshing" indicator
                tryPerformAutoRefresh()
            }
        }
    }

    private suspend fun tryPerformAutoRefresh() {
        // avoid overlapping automatic refreshes or manual-in-flight operations
        if (_isRefreshing.value || inFlight) return

        refreshMutex.withLock {
            // double-check inside lock
            if (_isRefreshing.value || inFlight) return

            performRefresh(showUiIndicator = false)
        }
    }

    fun refresh() {
        viewModelScope.launch {

            refreshMutex.withLock {

                performRefresh(showUiIndicator = true)
            }
        }
    }

    private suspend fun performRefresh(showUiIndicator: Boolean) {
        _isRefreshing.value = showUiIndicator

        _error.value = null

        currentPage = 0

        isLastPageReached = false

        try {
            _isLoading.value = true

            // call repo; repo may throw on network error (caught below)
            val fetched = try {
                repo.fetchAndCachePage(0, pageSize)
            } catch (t: Throwable) {
                // network error -> mark offline and keep cached data visible
                _isOffline.value = true
                _error.value = t.message ?: "Failed to refresh"
                return
            }

            // if we reached here, fetch succeeded (even if empty)
            _isOffline.value = false

            // if fetched is empty it might be no items on server; keep currentPage as 0
            if (fetched.isNotEmpty()) {
                // we fetched page 0 successfully; update page tracking if needed
                currentPage = 0
            }
        } finally {
            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun loadMore() {

        if (inFlight || isLastPageReached) return

        inFlight = true
        viewModelScope.launch {

            _error.value = null

            try {

                _isLoading.value = true

                val nextPage = currentPage + 1

                val fetched = try {
                    repo.fetchAndCachePage(nextPage, pageSize)
                } catch (t: Throwable) {

                    // network error while loading more -> mark offline
                    _isOffline.value = true
                    _error.value = t.message ?: "Failed to load more"
                    return@launch
                }

                _isOffline.value = false

                if (fetched.isEmpty()) {
                    isLastPageReached = true
                } else {
                    currentPage = nextPage
                }
            } catch (t: Throwable) {

                _error.value = t.message ?: "Failed to load more"
            } finally {

                _isLoading.value = false
                inFlight = false
            }
        }
    }
}