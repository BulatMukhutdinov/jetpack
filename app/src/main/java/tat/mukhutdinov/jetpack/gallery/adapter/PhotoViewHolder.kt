package tat.mukhutdinov.jetpack.gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import tat.mukhutdinov.jetpack.R
import tat.mukhutdinov.jetpack.databinding.PhotoBinding
import java.io.File

class PhotoViewHolder(
    private val binding: PhotoBinding,
    private val photoBindings: PhotoBindings
) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

    override val containerView: View = binding.root

    fun bind(file: File) {
        binding.binding = photoBindings
        binding.file = file

        binding.executePendingBindings()
    }

    companion object {

        fun create(parent: ViewGroup, photoBindings: PhotoBindings): PhotoViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: PhotoBinding = DataBindingUtil.inflate(layoutInflater, R.layout.photo, parent, false)

            return PhotoViewHolder(binding, photoBindings)
        }
    }
}
