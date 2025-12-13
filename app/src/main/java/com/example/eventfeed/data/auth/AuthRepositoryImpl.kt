package com.example.eventfeed.data.auth


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : AuthRepository {
    override suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val resp: LoginResponse = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }.body()

            resp.token?.let {
                Result.success(Unit)
            } ?: Result.failure(Exception(resp.error ?: "Invalid credentials"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}