package tat.mukhutdinov.jetpack.gallery

import kotlinx.coroutines.channels.ReceiveChannel
import tat.mukhutdinov.jetpack.gallery.api.GalleryDomain
import tat.mukhutdinov.jetpack.gallery.api.GalleryGateway
import tat.mukhutdinov.jetpack.infrastructure.util.DIVIDER
import java.io.File

internal class GalleryInteractor(private val galleryGateway: GalleryGateway) : GalleryDomain {

    private val filesLumas: MutableMap<String, String> by lazy { initFilesLumas() }

    private fun initFilesLumas(): MutableMap<String, String> {
        val filesLumas = mutableMapOf<String, String>()

        val byteArray = galleryGateway.getLumasFileContent()
        val content = String(byteArray)

        val entries = content.lines()

        entries.forEach {
            if (it.isNotEmpty()) {
                val entry = it.split(DIVIDER)
                filesLumas[entry[0]] = entry[1]
            }
        }

        return filesLumas
    }

    override fun observeAllPhotos(): ReceiveChannel<List<File>> =
        galleryGateway.observeAllPhotos()

    override fun delete(photo: File) {
        galleryGateway.delete(photo)

        filesLumas.remove(photo.name)

        val content = StringBuilder()

        filesLumas.forEach { key, value ->
            content.append("$key$DIVIDER$value${System.lineSeparator()}")
        }
    }

    override fun getFilesLuma(file: File): String {
        return filesLumas[file.name].orEmpty()
    }
}