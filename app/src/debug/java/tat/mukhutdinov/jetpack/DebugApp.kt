package tat.mukhutdinov.jetpack

import android.app.Application
import android.os.StrictMode
import com.facebook.stetho.Stetho
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class DebugApp : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()

        setupDomain()

        setupTimber()

        setupStetho()

        setupPicasso()

        setupUncaughtExceptions()

        setupStrictMode()
    }

    private fun setupDomain() {
        Domain.init()
    }

    private fun setupUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Timber.e(throwable)
        }
    }

    private fun setupPicasso() {
        val picasso = Picasso.Builder(this)
            .memoryCache(LruCache(this))
            .build()

        Picasso.setSingletonInstance(picasso)
    }

    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setupStetho() {
        Stetho.initializeWithDefaults(this)
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@DebugApp)
        }
    }
}
