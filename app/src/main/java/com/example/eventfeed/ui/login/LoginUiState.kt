package com.example.eventfeed.ui.login

data class LoginUiState(
    val success: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)