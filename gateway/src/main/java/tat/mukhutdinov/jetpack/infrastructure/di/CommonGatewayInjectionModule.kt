package tat.mukhutdinov.jetpack.infrastructure.di

import android.content.Context
import org.koin.dsl.module
import tat.mukhutdinov.jetpack.R
import tat.mukhutdinov.jetpack.infrastructure.common.FilesLocalGateway
import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import java.io.File

internal val commonInjectionModule = module {

    single {
        val context: Context = get()

        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        } else {
            context.filesDir
        }
    }

    factory<FilesGateway> {
        FilesLocalGateway(get(), get())
    }
}