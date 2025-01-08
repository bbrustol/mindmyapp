package com.bbrustol.mmm

import android.app.Application
import com.bbrustol.core.infrastructure.di.coroutinesDispatchersModule
import com.bbrustol.core.infrastructure.di.networkModule
import com.bbrustol.feature.organizations.di.organizationsPresenterModule
import com.bbrustol.mindmylib.di.mindMyLibModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MindMyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MindMyApplication)
            modules(
                networkModule,
                coroutinesDispatchersModule,
                mindMyLibModule,
                organizationsPresenterModule
            )
            androidLogger()
        }
    }
}