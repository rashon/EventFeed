package com.example.eventfeed.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String? = null,
    val error: String? = null
)