package com.example.yolo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.yolo.ml.BestFp16
import com.example.yolo.tflite.Classifier
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    companion object {
        const val MINIMUM_CONFIDENCE_TF_OD_API = 0.3f
    }

     lateinit var model: BestFp16
     lateinit var image: Bitmap
     lateinit var button: Button
     lateinit var imageView: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



         image = BitmapFactory.decodeStream(assets.open("testeimage.jpg"));

        button = findViewById(R.id.buttonId)
        imageView = findViewById(R.id.imageView)

        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                model = BestFp16.newInstance(applicationContext)
                image = Bitmap.createScaledBitmap(image,640,640, false)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 640, 640, 3), DataType.FLOAT32)
                 val byteBuffer = ByteBuffer.allocateDirect(4 * 640 * 640 * 3 );
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(640 * 640)
                image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                var pixel = 0
                for (i in 0 until 640) {
                    for (j in 0 until 640) {
                        val `val` = intValues[pixel++] // RGB
                        byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                        byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                        byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                    }
                }
                inputFeature0.loadBuffer(byteBuffer);

                val outputs = model.process(inputFeature0)

                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                val confidences = outputFeature0.getFloatArray()


                Log.d("dddddddddd", "eeee" + confidences)
                model.close()


            }
        })


        fun handleResult(bitmap: Bitmap, results: List<Classifier.Recognition>) {
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 2.0f
            }

            for (result in results) {
                val location = result.location
                if (location != null && result.confidence >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    canvas.drawRect(location, paint)
                }
            }

            imageView.setImageBitmap(image)
        }

    }






    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName!!).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }
}