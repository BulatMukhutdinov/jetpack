package tat.mukhutdinov.jetpack.gallery.di

import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import tat.mukhutdinov.jetpack.gallery.GalleryLocalGateway
import tat.mukhutdinov.jetpack.gallery.api.GalleryGateway

internal val galleryInjectionModule = module {

    factory<GalleryGateway> { (scope: CoroutineScope) ->
        GalleryLocalGateway(get(), get(), scope)
    }
}