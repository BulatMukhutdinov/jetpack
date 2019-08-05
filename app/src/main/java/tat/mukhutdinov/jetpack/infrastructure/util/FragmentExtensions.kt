package tat.mukhutdinov.jetpack.infrastructure.util

import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * In case we need some specific error handling for coroutine we can use exception handler
 * Example:
 * launch(coroutineExceptionHandler){
 *      dangerousMethod()
 * }
 * Note: Thread.defaultUncaughtExceptionHandler *WON"T* be fired
 */
fun Fragment.getCoroutineExceptionHandler() =
    CoroutineExceptionHandler { _, exception ->
        runBlocking(Dispatchers.Main) {
            Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }

        Timber.e(exception)
    }
