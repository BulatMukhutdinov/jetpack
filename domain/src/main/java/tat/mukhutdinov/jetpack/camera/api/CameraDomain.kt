package tat.mukhutdinov.jetpack.camera.api

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import java.io.File

interface CameraDomain {

    fun createFile(): File

    fun getLatestPhoto(): Bitmap?

    fun cropCircularThumbnail(bitmap: Bitmap, diameter: Int = 128): Bitmap

    fun saveFileLuma(file: File, luma: Double)

    fun decodeBitmap(file: File): Bitmap

    fun decodeBitmap(image: ImageProxy, rotationDegrees: Int): Bitmap
}