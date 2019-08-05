package tat.mukhutdinov.jetpack

import org.koin.core.context.loadKoinModules
import tat.mukhutdinov.jetpack.camera.di.cameraInjectionModule
import tat.mukhutdinov.jetpack.gallery.di.galleryInjectionModule
import tat.mukhutdinov.jetpack.infrastructure.di.commonInjectionModule

object Gateway {

    fun init() {
        loadKoinModules(listOf(commonInjectionModule, cameraInjectionModule, galleryInjectionModule))
    }
}