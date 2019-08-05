package tat.mukhutdinov.jetpack.gallery.api

import kotlinx.coroutines.channels.ReceiveChannel
import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import java.io.File

interface GalleryGateway : FilesGateway {

    fun observeAllPhotos(): ReceiveChannel<List<File>>

    fun delete(photo: File)
}