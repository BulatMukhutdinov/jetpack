package tat.mukhutdinov.jetpack.gallery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import tat.mukhutdinov.jetpack.gallery.api.GalleryGateway
import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import tat.mukhutdinov.jetpack.infrastructure.util.PHOTO_EXTENSION
import java.io.File

internal class GalleryLocalGateway(
    private val baseFolder: File,
    private val filesGateway: FilesGateway,
    private val scope: CoroutineScope
) : GalleryGateway, FilesGateway by filesGateway {

    private val photosChannel = Channel<List<File>>()

    init {
        refreshPhotos()
    }

    override fun delete(photo: File) {
        photo.delete()

        refreshPhotos()
    }

    @ObsoleteCoroutinesApi
    override fun observeAllPhotos(): ReceiveChannel<List<File>> =
        photosChannel

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun refreshPhotos() {
        val photos = baseFolder
            .listFiles { file -> PHOTO_EXTENSION.contains(file.extension.toLowerCase()) }
            .sorted()
            .reversed()

        scope.launch {
            photosChannel.send(photos)
        }
    }
}