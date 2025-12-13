package com.example.eventfeed.di

import com.example.eventfeed.data.auth.AuthRepository
import com.example.eventfeed.data.auth.AuthRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get(named("baseUrl")))
    }
}