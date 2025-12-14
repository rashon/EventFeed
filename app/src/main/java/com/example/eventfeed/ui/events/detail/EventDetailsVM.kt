package com.example.eventfeed.ui.events.detail

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
import kotlinx.coroutines.withContext

class EventDetailsVM(
    private val repo: EventsRepository,
    private val eventId: String
) : ViewModel() {

    private val pageSize = 20
    private val refreshIntervalMillis = 3_000L

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _eventTitle = MutableStateFlow<String>("Event")
    val eventTitle: StateFlow<String> = _eventTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Observe cached events and pick the one matching the provided id (reflection-based matcher).
        viewModelScope.launch {

            repo.cachedEventsFlow().collect { list ->

                val found = list.find { evt -> extractId(evt) == eventId }

                _event.value = found

                _eventTitle.value = extractTitle(found) ?: "Event"
            }
        }

        // initial attempt to refresh from network
        refresh()

        // start auto-refresh loop (runs on viewModelScope and cancels automatically)
        viewModelScope.launch {

            while (isActive) {

                delay(refreshIntervalMillis)

                // avoid overlapping refreshes
                if (!_isLoading.value) {
                    performRefresh()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            performRefresh()
        }
    }

    private suspend fun performRefresh() = withContext(Dispatchers.IO) {

        _isLoading.value = true

        _error.value = null

        try {
            try {

                repo.fetchAndCachePage(0, pageSize)

                _isOffline.value = false

            } catch (t: Throwable) {

                _isOffline.value = true

                _error.value = t.message ?: "Failed to refresh"
            }
        } finally {

            _isLoading.value = false
        }
    }

    private fun extractId(obj: Any?): String? {

        if (obj == null) return null

        val names = listOf("id", "eventId", "uid", "uuid")

        for (n in names) {
            try {

                val f = obj.javaClass.getDeclaredField(n)

                f.isAccessible = true

                val v = f.get(obj) ?: continue

                return v.toString()
            } catch (_: Throwable) { /* ignore and try next */
            }
        }

        try {

            val m = obj.javaClass.methods.firstOrNull { it.name.equals("getId", ignoreCase = true) }

            val res = m?.invoke(obj)

            if (res != null) return res.toString()

        } catch (_: Throwable) {
        }

        return null
    }

    private fun extractTitle(obj: Any?): String? {

        if (obj == null) return null

        val names = listOf("title", "name", "headline")

        for (n in names) {
            try {

                val f = obj.javaClass.getDeclaredField(n)

                f.isAccessible = true

                val v = f.get(obj) as? String

                if (!v.isNullOrBlank()) return v

            } catch (_: Throwable) { /* ignore and try next */
            }
        }
        try {

            val m = obj.javaClass.methods.firstOrNull {
                it.name.equals(
                    "getTitle",
                    ignoreCase = true
                ) || it.name.equals("getName", ignoreCase = true)
            }

            val res = m?.invoke(obj) as? String

            if (!res.isNullOrBlank()) return res

        } catch (_: Throwable) {
        }
        return null
    }
}