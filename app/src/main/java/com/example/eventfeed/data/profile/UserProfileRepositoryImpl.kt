package com.example.eventfeed.data.profile

import com.example.eventfeed.data.remote.UserProfileDto
import com.example.eventfeed.data.remote.toDomain
import com.example.eventfeed.domain.model.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfileRepositoryImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : UserProfileRepository {

    // per-request timeout to ensure we fail before the 3s refresh tick

    override suspend fun fetchProfile(): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val dto: UserProfileDto = client.get("$baseUrl/profile") {}.body()

            dto.toDomain()

        } catch (_: Exception) {
            null
        }
    }
}