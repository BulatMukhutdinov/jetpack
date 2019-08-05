package tat.mukhutdinov.jetpack.gallery.api

import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

interface GalleryDomain {

    fun observeAllPhotos(): ReceiveChannel<List<File>>

    fun delete(photo: File)

    fun getFilesLuma(file: File): String
}