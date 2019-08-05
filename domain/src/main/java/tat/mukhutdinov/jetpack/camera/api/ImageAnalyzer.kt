package tat.mukhutdinov.jetpack.camera.api

import androidx.camera.core.ImageAnalysis

interface ImageAnalyzer : ImageAnalysis.Analyzer {

    fun addOnFrameAnalyzedListener(listener: (luma: Double) -> Unit): Boolean

    fun clearOnFrameAnalyzedListeners()
}