package com.example.eventfeed.di

import com.example.eventfeed.data.auth.AuthRepository
import com.example.eventfeed.data.auth.AuthRepositoryImpl
import com.example.eventfeed.data.event.EventsRepository
import com.example.eventfeed.data.event.EventsRepositoryImpl
import com.example.eventfeed.data.profile.UserProfileRepository
import com.example.eventfeed.data.profile.UserProfileRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get(named("baseUrl")))
    }

    single<EventsRepository> { EventsRepositoryImpl(get(), get(named("baseUrl")), get()) }

    single<UserProfileRepository> { UserProfileRepositoryImpl(get(), get(named("baseUrl"))) }
}