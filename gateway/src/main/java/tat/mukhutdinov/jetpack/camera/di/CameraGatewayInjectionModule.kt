package tat.mukhutdinov.jetpack.camera.di

import org.koin.dsl.module
import tat.mukhutdinov.jetpack.camera.CameraLocalGateway
import tat.mukhutdinov.jetpack.camera.api.CameraGateway

internal val cameraInjectionModule = module {

    factory<CameraGateway> {
        CameraLocalGateway(get(), get())
    }
}