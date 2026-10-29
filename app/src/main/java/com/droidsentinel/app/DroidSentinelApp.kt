package com.droidsentinel.app

import android.app.Application
import com.droidsentinel.app.di.AppContainer

class DroidSentinelApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
