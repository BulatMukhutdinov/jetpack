package tat.mukhutdinov.jetpack.camera.api

import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import java.io.File

interface CameraGateway : FilesGateway {

    fun createFile(): File

    fun getLatestPhoto(): File?
}