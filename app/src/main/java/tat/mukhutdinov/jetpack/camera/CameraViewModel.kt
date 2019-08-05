package tat.mukhutdinov.jetpack.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.MimeTypeMap
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CaptureMode
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import tat.mukhutdinov.jetpack.R
import tat.mukhutdinov.jetpack.camera.api.CameraDomain
import tat.mukhutdinov.jetpack.camera.api.ImageAnalyzer
import tat.mukhutdinov.jetpack.databinding.CameraBinding
import tat.mukhutdinov.jetpack.infrastructure.structure.BaseViewModel
import tat.mukhutdinov.jetpack.infrastructure.util.AutoFitPreviewBuilder
import tat.mukhutdinov.jetpack.infrastructure.util.getCoroutineExceptionHandler
import timber.log.Timber
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat

class CameraViewModel : BaseViewModel(), CameraBindings {

    override val luminosity = MutableLiveData<String>()

    private val cameraDomain: CameraDomain by inject()

    private val lumaAnalyzer: ImageAnalyzer by inject()

    private var displayId = -1
    private var lensFacing = CameraX.LensFacing.BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private val coroutineExceptionHandler = getCoroutineExceptionHandler()

    private var currentLuma = 0.0
    private var lumaOnPhotoTakeQueue = Channel<Double>(10)

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {

        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            view?.let {
                if (displayId == this@CameraViewModel.displayId) {
                    preview?.setTargetRotation(it.display.rotation)
                    imageCapture?.setTargetRotation(it.display.rotation)
                    imageAnalyzer?.setTargetRotation(it.display.rotation)
                }
            }
        }
    }

    /** Define callback that will be triggered after a photo has been taken and saved to disk */
    private val onImageSavedListener = object : ImageCapture.OnImageSavedListener {

        override fun onImageSaved(photoFile: File) {
            launch(coroutineExceptionHandler) {
                val bitmap = cameraDomain.decodeBitmap(photoFile)
                setGalleryThumbnail(bitmap)

                cameraDomain.saveFileLuma(photoFile, lumaOnPhotoTakeQueue.receive())
            }

            // If the folder selected is an external media directory, this is unnecessary
            // but otherwise other apps will not be able to access our images unless we
            // scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
        }

        override fun onError(error: ImageCapture.UseCaseError, message: String, throwable: Throwable?) {
            Timber.e(throwable)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val cameraBinding: CameraBinding = DataBindingUtil.inflate(inflater, R.layout.camera, container, false)

        cameraBinding.binding = this
        cameraBinding.lifecycleOwner = viewLifecycleOwner

        return cameraBinding.root
    }

    private fun setGalleryThumbnail(bitmap: Bitmap) {
        launch(coroutineExceptionHandler) {
            val circularImage = cameraDomain.cropCircularThumbnail(bitmap)

            withContext(Dispatchers.Main) {
                gallery.foreground = BitmapDrawable(resources, circularImage)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        showLatestPhoto()

        // Wait for the views to be properly laid out
        texture.post {
            displayId = texture.display.displayId

            bindCameraUseCases()
        }
    }

    private fun showLatestPhoto() {
        launch(coroutineExceptionHandler) {
            cameraDomain.getLatestPhoto()?.let { setGalleryThumbnail(it) }
        }
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { texture.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                // We request aspect ratio but no resolution to let CameraX optimize our use cases
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(texture.display.rotation)
            }
            .build()

        preview = AutoFitPreviewBuilder.build(previewConfig, texture)

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setCaptureMode(CaptureMode.MIN_LATENCY)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(texture.display.rotation)
            }
            .build()

        imageCapture = ImageCapture(imageCaptureConfig)

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
                setCallbackHandler(Handler(analyzerThread.looper))
                // In our analysis, we care more about the latest image than analyzing *every* image
                setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                setTargetRotation(texture.display.rotation)
            }
            .build()

        imageAnalyzer = ImageAnalysis(imageAnalysisConfig).apply {
            analyzer = lumaAnalyzer.apply {
                addOnFrameAnalyzedListener { luma ->
                    currentLuma = luma

                    val decimalFormat = DecimalFormat("#.##")
                    decimalFormat.roundingMode = RoundingMode.CEILING

                    luminosity.postValue(getString(R.string.luma, decimalFormat.format(luma)))
                }
            }
        }

        //https://github.com/android/camera/issues/19
        CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture, imageAnalyzer)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.unregisterDisplayListener(displayListener)

        lumaAnalyzer.clearOnFrameAnalyzedListeners()
    }

    override fun onCaptureClicked() {
        val photoFile = cameraDomain.createFile()

        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
        }

        launch(coroutineExceptionHandler) {
            lumaOnPhotoTakeQueue.send(currentLuma)
            imageCapture?.takePicture(photoFile, onImageSavedListener, metadata)

            flash()
        }
    }

    private fun flash() {
        launch(coroutineExceptionHandler) {
            withContext(Dispatchers.Main) {
                root.foreground = ColorDrawable(Color.WHITE)
                delay(50)
                root.foreground = null
            }
        }
    }

    override fun onGalleryClicked() {
        findNavController().navigate(CameraViewModelDirections.toGallery())
    }

    @SuppressLint("RestrictedApi")
    override fun onSwitchClicked() {
        rotateSwitchIcon()

        blurPreview()

        lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
            CameraX.LensFacing.BACK
        } else {
            CameraX.LensFacing.FRONT
        }

        try {
            // Only bind use cases if we can query a camera with this orientation
            CameraX.getCameraWithLensFacing(lensFacing)

            CameraX.unbindAll()

            bindCameraUseCases()
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

    private fun blurPreview() {
        Blurry.with(context)
            .radius(15)
            .sampling(10)
            .from(texture.bitmap)
            .into(blur)

        launch(coroutineExceptionHandler + Dispatchers.Main) {
            delay(1000)
            blur.setImageDrawable(null)
        }
    }

    private fun rotateSwitchIcon() {
        val rotate = RotateAnimation(
            0f, -180f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 700
        rotate.interpolator = AccelerateDecelerateInterpolator()
        // keep rotation after animation
        rotate.fillAfter = true
        switchSide.startAnimation(rotate)
    }
}