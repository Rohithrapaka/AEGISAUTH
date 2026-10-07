package com.aegisauth

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AegisApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
