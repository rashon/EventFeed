package com.example.eventfeed

import android.app.Application
import com.example.eventfeed.di.dbModule
import com.example.eventfeed.di.networkModule
import com.example.eventfeed.di.repositoryModule
import com.example.eventfeed.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(networkModule, dbModule, repositoryModule, viewModelModule))
        }
    }
}
