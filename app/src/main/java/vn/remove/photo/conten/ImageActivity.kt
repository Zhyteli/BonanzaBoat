package vn.remove.photo.conten

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.remove.photo.conten.databinding.ActivityImageBinding
import kotlin.random.Random

class ImageActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityImageBinding.inflate(layoutInflater)
    }
    private val im = listOf(
        R.drawable.dage,
        R.drawable.gou,
        R.drawable.welo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        randImage()
        binding.whe.setOnClickListener {
            ObjectAnimator.ofFloat (binding.clous,
                View.TRANSLATION_Y, 0f, 5000f).apply {
                duration = 2000
                interpolator = LinearInterpolator()

                start()
            }
            ObjectAnimator.ofFloat (binding.clous,
                View.TRANSLATION_Y, 5000f, 0f).apply {
                duration = 2000
                interpolator = LinearInterpolator()

                start()
            }
        }
    }
    private fun randImage(){
        val image1 = im[im.indices.random(Random(System.currentTimeMillis() + 30))]
        val image2 = im[im.indices.random(Random(System.currentTimeMillis() + 20))]
        val image3 = im[im.indices.random(Random(System.currentTimeMillis() + 10))]

        binding.dag.setImageResource(image1)
        binding.gou.setImageResource(image2)
        binding.welo.setImageResource(image3)

        lifecycleScope.launch {
            delay(5000)
            randImage()
        }
    }

}