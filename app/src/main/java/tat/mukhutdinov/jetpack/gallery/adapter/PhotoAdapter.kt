package tat.mukhutdinov.jetpack.gallery.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PhotoAdapter(private val photoBindings: PhotoBindings) : RecyclerView.Adapter<PhotoViewHolder>() {

    val photos: MutableList<File> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder.create(parent, photoBindings)

    override fun getItemCount(): Int =
        photos.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemId(position: Int): Long =
        photos[position].path.hashCode().toLong()
}