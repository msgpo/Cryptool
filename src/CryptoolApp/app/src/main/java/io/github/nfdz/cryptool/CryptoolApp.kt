package io.github.nfdz.cryptool

import android.app.Application
import timber.log.Timber


class CryptoolApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupLogger()
    }

    private fun setupLogger() {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())
        }
    }

}