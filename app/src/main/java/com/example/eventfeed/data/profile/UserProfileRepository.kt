package com.example.eventfeed.data.profile

import com.example.eventfeed.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun fetchProfile(): UserProfile?
}