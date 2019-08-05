package tat.mukhutdinov.jetpack.gallery.di

import kotlinx.coroutines.CoroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import tat.mukhutdinov.jetpack.gallery.GalleryInteractor
import tat.mukhutdinov.jetpack.gallery.api.GalleryDomain

internal val galleryInjectionModule = module {

    factory<GalleryDomain> { (scope: CoroutineScope) ->
        GalleryInteractor(get { parametersOf(scope) })
    }
}
