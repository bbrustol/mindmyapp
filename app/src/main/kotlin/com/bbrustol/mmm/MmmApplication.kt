package com.bbrustol.mmm

import android.app.Application
import com.bbrustol.core.infrastructure.di.coroutinesDispatchersModule
import com.bbrustol.core.infrastructure.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MmmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MmmApplication)
            modules(networkModule, coroutinesDispatchersModule)
            androidLogger()
        }
    }
}