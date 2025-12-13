package com.example.eventfeed.data.auth

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Unit>
}