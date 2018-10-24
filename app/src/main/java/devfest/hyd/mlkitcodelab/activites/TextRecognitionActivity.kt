package devfest.hyd.mlkitcodelab.activites

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import devfest.hyd.mlkitcodelab.R
import devfest.hyd.mlkitcodelab.utils.askRuntimePermission
import kotlinx.android.synthetic.main.layout_vision.*
import java.io.IOException

class TextRecognitionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_rec)

        btSelectImage.setOnClickListener {
            askRuntimePermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            try {
                val uri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

                fetchTextFromImage(bitmap)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    /* Recognize text from an image */
    private fun fetchTextFromImage(bitmap: Bitmap) {

        /**
         * An image object that is created from either a bitmap, media.Image, ByteBuffer, Byte array or file on the
         * device which will be used for on device and cloud API detectors
         */
        val vision: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)

        /* Create text detector on the instance of firebase vision */
        val recognizer: FirebaseVisionTextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        /* Pass firebase vision image to detector's process image method */
        recognizer.processImage(vision)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    tvExtractedText.text = it.result?.text
                } else {
                    Toast.makeText(
                        this@TextRecognitionActivity,
                        getString(R.string.toast_un_able_to_extract_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}