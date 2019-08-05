package tat.mukhutdinov.jetpack.infrastructure.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import java.io.File

@BindingAdapter("file")
fun setFile(image: ImageView, file: File) {
    Picasso.get()
            .load(file)
            .into(image)
}