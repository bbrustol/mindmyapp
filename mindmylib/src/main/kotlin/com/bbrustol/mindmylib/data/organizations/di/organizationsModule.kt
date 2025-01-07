package com.bbrustol.mindmylib.data.organizations.di

import com.bbrustol.mindmylib.data.organizations.data.repository.OrganizationsRepository
import com.bbrustol.mindmylib.data.organizations.data.service.OrganizationsService
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

val mindMyLibModule = module {

    single {
        OrganizationsRepository(
            organizationsService = get(),
            dispatcher = get(StringQualifier("IoDispatcher"))
        )
    }
    factory { OrganizationsService(httpClient = get()) }
    
}