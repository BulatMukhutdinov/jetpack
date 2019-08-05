package tat.mukhutdinov.jetpack.infrastructure.container

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import tat.mukhutdinov.jetpack.R
import tat.mukhutdinov.jetpack.infrastructure.util.FLAGS_FULLSCREEN

class ContainerActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container)
        container = findViewById(R.id.container)
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    companion object {
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L
    }
}
