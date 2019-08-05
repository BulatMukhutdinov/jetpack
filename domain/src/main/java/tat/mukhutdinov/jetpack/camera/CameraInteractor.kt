package tat.mukhutdinov.jetpack.camera

import android.graphics.*
import android.media.ThumbnailUtils
import androidx.camera.core.ImageProxy
import androidx.exifinterface.media.ExifInterface
import tat.mukhutdinov.jetpack.camera.api.CameraDomain
import tat.mukhutdinov.jetpack.camera.api.CameraGateway
import tat.mukhutdinov.jetpack.infrastructure.util.DIVIDER
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat

internal class CameraInteractor(private val gateway: CameraGateway) : CameraDomain {

    override fun createFile(): File =
        gateway.createFile()

    override fun getLatestPhoto(): Bitmap? =
        gateway.getLatestPhoto()
            ?.let { decodeBitmap(it) }

    override fun saveFileLuma(file: File, luma: Double) {
        val byteArray = gateway.getLumasFileContent()
        val content = String(byteArray)

        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.roundingMode = RoundingMode.CEILING

        val newEntry = "${file.name}$DIVIDER${decimalFormat.format(luma)}"

        val newContent = content.plus(newEntry).plus(System.lineSeparator())

        gateway.saveLumasFile(newContent)
    }

    /**
     * This function cuts out a circular thumbnail from the provided bitmap. This is done by
     * first scaling the image down to a square with width of [diameter], and then marking all
     * pixels outside of the inner circle as transparent.
     *
     * @param bitmap - The [Bitmap] to be taken a thumbnail of
     * @param diameter - Size in pixels for the diameter of the resulting circle
     */
    override fun cropCircularThumbnail(bitmap: Bitmap, diameter: Int): Bitmap {
        // Extract a much smaller bitmap to serve as thumbnail
        val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, diameter, diameter)

        // Create an additional bitmap of same size as thumbnail to carve a circle out of
        val circular = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)

        // Paint will be used as a mask to cut out the circle
        val paint = Paint().apply {
            color = Color.BLACK
        }

        Canvas(circular).apply {
            drawARGB(0, 0, 0, 0)
            drawCircle(diameter / 2F, diameter / 2F, diameter / 2F - 8, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val rect = Rect(0, 0, diameter, diameter)
            drawBitmap(thumbnail, rect, rect, paint)
        }

        return circular
    }

    override fun decodeBitmap(image: ImageProxy, rotationDegrees: Int): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).rotate(rotationDegrees.toFloat())
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /**
     * Decode a bitmap from a file and apply the transformations described in its EXIF data
     *
     * @param file - The image file to be read using [BitmapFactory.decodeFile]
     */
    override fun decodeBitmap(file: File): Bitmap {
        // First, decode EXIF data and retrieve transformation matrix
        val exif = ExifInterface(file.absolutePath)
        val transformation = decodeExifOrientation(
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
            )
        )

        // Read bitmap using factory methods, and transform it using EXIF data
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        return Bitmap.createBitmap(
            BitmapFactory.decodeFile(file.absolutePath),
            0,
            0,
            bitmap.width,
            bitmap.height,
            transformation,
            true
        )
    }

    /**
     * Helper function used to convert an EXIF orientation enum into a transformation matrix
     * that can be applied to a bitmap.
     *
     * @param orientation - One of the constants from [ExifInterface]
     */
    private fun decodeExifOrientation(orientation: Int): Matrix {
        val matrix = Matrix()

        // Apply transformation corresponding to declared EXIF orientation
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> Unit
            ExifInterface.ORIENTATION_UNDEFINED -> Unit
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(270F)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(90F)
            }

            // Error out if the EXIF orientation is invalid
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }

        // Return the resulting matrix
        return matrix
    }
}