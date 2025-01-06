package com.bbrustol.core.infrastructure.di

import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

val coroutinesDispatchersModule = module {
    single(qualifier = StringQualifier("DefaultDispatcher")) { Dispatchers.Default }
    single(qualifier = StringQualifier("IoDispatcher")) { Dispatchers.IO }
    single(qualifier = StringQualifier("MainDispatcher")) { Dispatchers.Main }
    single(qualifier = StringQualifier("MainImmediateDispatcher")) { Dispatchers.Main.immediate }
    single(qualifier = StringQualifier("Unconfined")) { Dispatchers.Unconfined }
}