package tat.mukhutdinov.jetpack

import org.koin.core.context.loadKoinModules
import tat.mukhutdinov.jetpack.camera.di.cameraInjectionModule
import tat.mukhutdinov.jetpack.gallery.di.galleryInjectionModule

object Domain {

    fun init() {
        Gateway.init()

        loadKoinModules(listOf(cameraInjectionModule, galleryInjectionModule))
    }
}