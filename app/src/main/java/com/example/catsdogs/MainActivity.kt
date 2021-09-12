package com.example.catsdogs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.catsdogs.ml.Model
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var selectGallery: Button
    lateinit var predict: Button
    lateinit var textView: TextView
    lateinit var bitmap: Bitmap
    lateinit var selectCamera: Button
    lateinit var progressBar: ProgressBar


    companion object{
        val IMG_REQUEST_CODE = 1001
        lateinit var img: Bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        selectGallery = findViewById(R.id.imageGallery)
        predict = findViewById(R.id.predictImage)
        textView = findViewById(R.id.textViw)
        selectCamera = findViewById(R.id.imageCamera)
        progressBar = findViewById(R.id.progressBar)

        progressBar.isVisible = false
        selectCamera.isEnabled = false
    }

    fun selectImageGallery(view: android.view.View) {
        // CODE TO SELECT AN IMAGE FROM THE GALLERY
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)

        // ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("image/*")).crop()
        //    .maxResultSize(400, 400).start()
    }

    fun predictImage(view: android.view.View)  {

        progressBar.visibility = View.VISIBLE

        GlobalScope.launch {
            img = Bitmap.createScaledBitmap(img, 150, 150, true)

            val model = Model.newInstance(applicationContext)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)

            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(img)
            val byteBuffer = tensorImage.buffer


            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Releases model resources if no longer used.
            model.close()

            if (outputFeature0.floatArray[0].toInt() == 1) // 1 is DOG, 0 is CAT
            {
                textView.text = "WOOF! WOOF! It's a DOG"
                progressBar.visibility = View.INVISIBLE
            }
            else{
                textView.text = "MEOW! MEOW! It's a CAT"
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IMG_REQUEST_CODE && resultCode == RESULT_OK)
        {
            imageView.setImageURI(data?.data)

            val uri = data?.data
            img = getBitmap(this.contentResolver, uri)
            //progressBar.visibility = View.VISIBLE
        }

    }

    fun selectImageCamera(view: android.view.View) {
    }
}