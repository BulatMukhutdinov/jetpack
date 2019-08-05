package tat.mukhutdinov.jetpack.camera

import tat.mukhutdinov.jetpack.camera.api.CameraGateway
import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import tat.mukhutdinov.jetpack.infrastructure.util.PHOTO_EXTENSION
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal class CameraLocalGateway(
    private val baseFolder: File,
    private val filesGateway: FilesGateway
) : CameraGateway, FilesGateway by filesGateway {

    private val simpleDateFormat = SimpleDateFormat(FILENAME, Locale.US)

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun getLatestPhoto(): File? =
        baseFolder
            .listFiles { file -> PHOTO_EXTENSION.contains(file.extension.toLowerCase()) }
            .sorted()
            .reversed()
            .firstOrNull()

    override fun createFile(): File =
        File(baseFolder, "${simpleDateFormat.format(System.currentTimeMillis())}.$PHOTO_EXTENSION")

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}