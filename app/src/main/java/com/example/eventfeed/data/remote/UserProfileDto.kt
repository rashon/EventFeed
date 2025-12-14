package com.example.eventfeed.data.remote

import com.example.eventfeed.domain.model.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

fun UserProfileDto.toDomain(): UserProfile =
    UserProfile(
        id = id,
        name = name,
        email = email,
        bio = bio,
        avatarUrl = avatarUrl
    )