package com.bbrustol.core.infrastructure.di

import android.net.http.HttpResponseCache.install
import com.bbrustol.core.infrastructure.network.DefaultNetworkChecker
import com.bbrustol.core.infrastructure.network.NetworkChecker
import com.bbrustol.feature.BuildConfig
import com.bbrustol.feature.data.repository.PokemonRepository
import com.bbrustol.feature.data.service.PokemonService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

val networkModule = module {
    single<NetworkChecker> { DefaultNetworkChecker(context = androidContext()) }
    single { provideHttpClient() }
    single {
        PokemonRepository(
            pokemonService = get(),
            dispatcher = get(StringQualifier("IoDispatcher"))
        )
    }
    factory { PokemonService(httpClient = get(), networkChecker = get()) }
}

private fun provideHttpClient(): HttpClient =
    HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = BuildConfig.BASE_URL
                header("Content-Type", "application/json")
            }
        }

        install(Logging) {
            val isDebug: Boolean = BuildConfig.DEBUG
            level = if (isDebug) LogLevel.ALL else LogLevel.INFO
            logger = if (isDebug) Logger.DEFAULT else Logger.EMPTY
        }

        engine {
            connectTimeout = 15_000
        }
    }