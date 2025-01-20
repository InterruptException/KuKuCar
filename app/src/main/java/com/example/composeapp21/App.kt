package com.example.composeapp21

import android.app.Application
import android.content.Context
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = SoftReference<Context>(this)
    }

    companion object {
        private var appContext = SoftReference<Context>(null)
        val app : Context
            get() {
                return appContext.get()!!
            }
    }
}