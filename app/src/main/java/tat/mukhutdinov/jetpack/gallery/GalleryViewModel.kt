package tat.mukhutdinov.jetpack.gallery

import android.animation.TimeInterpolator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.gallery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import tat.mukhutdinov.jetpack.BuildConfig
import tat.mukhutdinov.jetpack.R
import tat.mukhutdinov.jetpack.databinding.GalleryBinding
import tat.mukhutdinov.jetpack.gallery.adapter.PhotoAdapter
import tat.mukhutdinov.jetpack.gallery.adapter.PhotoBindings
import tat.mukhutdinov.jetpack.gallery.adapter.PhotoDiffUtilCallback
import tat.mukhutdinov.jetpack.gallery.api.GalleryDomain
import tat.mukhutdinov.jetpack.infrastructure.structure.BaseViewModel
import tat.mukhutdinov.jetpack.infrastructure.util.dpToPx
import tat.mukhutdinov.jetpack.infrastructure.util.getCoroutineExceptionHandler
import java.io.File
import kotlin.math.abs

class GalleryViewModel : BaseViewModel(), GalleryBindings, PhotoBindings {

    private val galleryDomain: GalleryDomain by inject { parametersOf(this) }

    private val coroutineExceptionHandler = getCoroutineExceptionHandler()

    private val photosChannel = galleryDomain.observeAllPhotos()

    private val adapter = PhotoAdapter(this)

    private var isControlsShown = true

    private val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            updateTitle(position)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val galleryBinding: GalleryBinding = DataBindingUtil.inflate(inflater, R.layout.gallery, container, false)

        galleryBinding.binding = this
        galleryBinding.lifecycleOwner = viewLifecycleOwner

        return galleryBinding.root
    }

    @ObsoleteCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        photos.unregisterOnPageChangeCallback(pageChangeListener)
    }

    private fun updateTitle(position: Int) {
        if (position < adapter.photos.size) {
            launch(coroutineExceptionHandler) {
                val luma = galleryDomain.getFilesLuma(adapter.photos[position])

                withContext(Dispatchers.Main) {
                    title.text = getString(R.string.luma, luma)
                }
            }
        } else {
            title.text = null
        }
    }

    @ObsoleteCoroutinesApi
    private fun setupAdapter() {
        photos.setPageTransformer { page, position ->
            val absPos = abs(position)
            page.apply {
                translationX = absPos * 350f
                scaleX = 1f
                scaleY = 1f
            }
        }

        photos.orientation = ViewPager2.ORIENTATION_VERTICAL
        photos.adapter = adapter

        photos.registerOnPageChangeCallback(pageChangeListener)

        launch(coroutineExceptionHandler) {
            photosChannel.consumeEach {
                withContext(Dispatchers.Main) {
                    val diffUtilCallback = PhotoDiffUtilCallback(adapter.photos, it)
                    val diffResult = DiffUtil.calculateDiff(diffUtilCallback)

                    adapter.photos.clear()
                    adapter.photos.addAll(it)
                    diffResult.dispatchUpdatesTo(adapter)

                    updateTitle(photos.currentItem)
                }
            }
        }
    }

    private fun animateControl(view: View, translationMultiplier: Int, alpha: Float, interpolator: TimeInterpolator) {
        view.animate()
            .translationY(dpToPx(36f, resources) * translationMultiplier)
            .alpha(alpha)
            .setInterpolator(interpolator)
            .setDuration(200)
            .start()
    }

    override fun onBackClicked() {
        findNavController().navigateUp()
    }

    override fun onDeleteClicked() {
        adapter.photos.getOrNull(photos.currentItem)?.let {
            launch(coroutineExceptionHandler) {
                galleryDomain.delete(it)
            }
        }
    }

    override fun onShareClicked() {
        val appContext = requireContext().applicationContext

        adapter.photos.getOrNull(photos.currentItem)?.let { file ->
            val intent = Intent().apply {
                // Infer media type from file extension
                val mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

                // Get URI from our FileProvider implementation
                val uri = FileProvider.getUriForFile(appContext, BuildConfig.APPLICATION_ID + ".provider", file)

                // Set the appropriate intent extra, type, action and flags
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mediaType
                action = Intent.ACTION_SEND
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
        }
    }

    override fun onImageClicked(file: File) {
        if (isControlsShown) {
            animateControl(back, -1, 0f, AccelerateInterpolator())
            animateControl(title, -1, 0f, AccelerateInterpolator())

            animateControl(delete, 1, 0f, AccelerateInterpolator())
            animateControl(share, 1, 0f, AccelerateInterpolator())
        } else {
            animateControl(back, 0, 1f, DecelerateInterpolator())
            animateControl(title, 0, 1f, DecelerateInterpolator())

            animateControl(delete, 0, 1f, DecelerateInterpolator())
            animateControl(share, 0, 1f, DecelerateInterpolator())
        }

        isControlsShown = !isControlsShown
    }
}