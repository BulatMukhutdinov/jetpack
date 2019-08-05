package tat.mukhutdinov.jetpack.camera.di

import org.koin.dsl.module
import tat.mukhutdinov.jetpack.camera.CameraInteractor
import tat.mukhutdinov.jetpack.camera.LuminosityAnalyzer
import tat.mukhutdinov.jetpack.camera.api.CameraDomain
import tat.mukhutdinov.jetpack.camera.api.ImageAnalyzer

internal val cameraInjectionModule = module {

    factory<CameraDomain> {
        CameraInteractor(get())
    }

    factory<ImageAnalyzer> {
        LuminosityAnalyzer()
    }
}
