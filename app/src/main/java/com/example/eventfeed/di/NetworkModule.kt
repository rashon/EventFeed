package com.example.eventfeed.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val baseUrl = "http://10.0.2.2:8080"

val networkModule = module {
    single {
        HttpClient(Android) {

            // Reduce default request wait time so exception is thrown before your refresh interval
            install(HttpTimeout) {
                requestTimeoutMillis = 2_500    // overall request timeout (< 3000ms)
                connectTimeoutMillis = 2_500    // TCP connect timeout
                socketTimeoutMillis = 2_500     // read/write socket timeout
            }

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.BODY
            }
        }
    }

    single(named("baseUrl")) { baseUrl }
}