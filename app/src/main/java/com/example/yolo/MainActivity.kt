package com.example.yolo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        try {
            val bitmap = BitmapFactory.decodeStream(assets.open("image.png"));
            val moduleFilePath = assetFilePath(this, "yolov5s.pt");
            val module = Module.load(moduleFilePath)

            val expectedWidth = 320
            val expectedHeight = 320

            val resizedBitmap =
                Bitmap.createScaledBitmap(bitmap, expectedWidth, expectedHeight, false)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

//            val (x, boxes, scores) = module.forward(IValue.from(inputTensor)).toTuple()
//





            val imageView = findViewById<ImageView>(R.id.imageView)
            imageView.setImageBitmap(bitmap)

            Log.e("Successzz", "Success")
        } catch (e: IOException) {
            Log.e("Error", "Error reading assets", e)
            finish()
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