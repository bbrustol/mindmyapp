package com.bbrustol.core.infrastructure.di

import com.bbrustol.core.infrastructure.BuildConfig
import com.bbrustol.core.infrastructure.network.DefaultNetworkChecker
import com.bbrustol.core.infrastructure.network.NetworkChecker
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { provideHttpClient() }
    single<NetworkChecker> { DefaultNetworkChecker(context = androidContext()) }
}

fun provideHttpClient(): HttpClient =
    HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = BuildConfig.BASE_URL
                header("Content-Type", "application/json")
                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.API_TOKEN}")
            }
        }

        install(Logging) {
            val isDebug: Boolean = BuildConfig.DEBUG
            level = if (isDebug) LogLevel.ALL else LogLevel.INFO
            logger = if (isDebug) Logger.ANDROID else Logger.EMPTY
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        engine {
            connectTimeout = 15_000
        }
    }
