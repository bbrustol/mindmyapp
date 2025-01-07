package com.bbrustol.feature.organizations.di

import com.bbrustol.feature.organizations.presentation.OrganizationsPresenter
import org.koin.dsl.module

val organizationsPresenterModule = module {
    factory { OrganizationsPresenter(get()) }
}