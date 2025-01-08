package com.bbrustol.mindmylib.di

import com.bbrustol.mindmylib.organization.data.local.database.FavoriteDatabase
import com.bbrustol.mindmylib.organization.data.repository.OrganizationsRepository
import com.bbrustol.mindmylib.organization.data.remote.service.OrganizationsService
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

val mindMyLibModule = module {
    single { FavoriteDatabase.getInstance(androidContext()).favoriteDao() }

    single {
        OrganizationsRepository(
            organizationsService = get(),
            favoriteDao = get(),
            dispatcher = get(StringQualifier("IoDispatcher"))
        )
    }

    factory {
        val client = get<HttpClient>()
        OrganizationsService(httpClient = client)
    }
}