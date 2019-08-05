package tat.mukhutdinov.jetpack

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()

        setupDomain()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Timber.e(throwable)
        }
    }

    private fun setupDomain() {
        Domain.init()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
        }
    }
}