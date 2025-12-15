package com.example.eventfeed.ui.profile

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventfeed.data.profile.UserProfileRepository
import com.example.eventfeed.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Stable
class UserProfileVM(
    private val repo: UserProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {

        viewModelScope.launch {

        performRefresh()
        }
    }

    private fun performRefresh() = viewModelScope.launch {

        _isLoading.value = true

        _error.value = null

        try {
            val p = try {
                repo.fetchProfile()
            } catch (t: Throwable) {
                null
            }

            if (p != null) {
                _profile.value = p
            } else {
                _error.value = "Unable to load profile"
            }

        } finally {
            _isLoading.value = false
        }
    }
}