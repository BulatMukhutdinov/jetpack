package tat.mukhutdinov.jetpack.camera

import androidx.camera.core.ImageProxy
import tat.mukhutdinov.jetpack.camera.api.ImageAnalyzer
import java.nio.ByteBuffer
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

/**
 * Our custom image analysis class.
 *
 * <p>All we need to do is override the function `analyze` with our desired operations. Here,
 * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
 */
internal class LuminosityAnalyzer : ImageAnalyzer {

    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<(luma: Double) -> Unit>()
    private var lastAnalyzedTimestamp = 0L
    private var framesPerSecond: Double = -1.0

    /**
     * Used to add listeners that will be called with each luma computed
     */
    override fun addOnFrameAnalyzedListener(listener: (luma: Double) -> Unit) = listeners.add(listener)

    override fun clearOnFrameAnalyzedListeners() {
        listeners.clear()
    }

    /**
     * Helper extension function used to extract a byte array from an image plane buffer
     */
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    /**
     * Analyzes an image to produce a result.
     *
     * <p>The caller is responsible for ensuring this analysis method can be executed quickly
     * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
     * images will not be acquired and analyzed.
     *
     * <p>The image passed to this method becomes invalid after this method returns. The caller
     * should not store external references to this image, as these references will become
     * invalid.
     *
     * @param image image being analyzed VERY IMPORTANT: do not close the image, it will be
     * automatically closed after this method returns
     * @return the image analysis result
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) return

        // Keep track of frames analyzed
        frameTimestamps.push(System.currentTimeMillis())

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        framesPerSecond = 1.0 / ((frameTimestamps.peekFirst() - frameTimestamps.peekLast()) / frameTimestamps.size.toDouble()) * 1000.0

        // Calculate the average luma no more often than every second
        if (frameTimestamps.first - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
            // Since format in ImageAnalysis is YUV, image.planes[0] contains the Y
            // (luminance) plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            lastAnalyzedTimestamp = frameTimestamps.first
        }
    }
}