package tat.mukhutdinov.jetpack.gallery.adapter

import androidx.recyclerview.widget.DiffUtil
import java.io.File

class PhotoDiffUtilCallback(private val oldList: List<File>, private val newList: List<File>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].path == newList[newItemPosition].path

    override fun getOldListSize(): Int =
        oldList.size

    override fun getNewListSize(): Int =
        newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}