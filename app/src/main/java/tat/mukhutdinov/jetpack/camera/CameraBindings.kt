package tat.mukhutdinov.jetpack.camera

import androidx.lifecycle.LiveData

interface CameraBindings {

    val luminosity: LiveData<String>

    fun onCaptureClicked()

    fun onGalleryClicked()

    fun onSwitchClicked()
}