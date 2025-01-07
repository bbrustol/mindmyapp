package com.bbrustol.mindmylib.data.organizations.di

import android.util.Log
import com.bbrustol.mindmylib.data.organizations.data.repository.OrganizationsRepository
import com.bbrustol.mindmylib.data.organizations.data.service.OrganizationsService
import io.ktor.client.HttpClient
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

val mindMyLibModule = module {
    single {
        OrganizationsRepository(
            organizationsService = get(),
            dispatcher = get(StringQualifier("IoDispatcher"))
        )
    }


    factory {
        val client = get<HttpClient>()
        OrganizationsService(httpClient = client)
    }
    
}