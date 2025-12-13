package com.example.eventfeed.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventfeed.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginVM(private val repo: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _ui.value = LoginUiState(isLoading = true)
            val result = repo.login(username, password)
            if (result.isSuccess) {
                _ui.value = LoginUiState(success = true)
            } else {
                val message = result.exceptionOrNull()?.message ?: "Login failed"
                _ui.value = LoginUiState(error = message)
            }
        }
    }

    fun resetUiState() {
        _ui.value = LoginUiState()
    }
}