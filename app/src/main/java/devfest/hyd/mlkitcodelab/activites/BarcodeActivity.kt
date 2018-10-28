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
import devfest.hyd.mlkitcodelab.R
import devfest.hyd.mlkitcodelab.utils.askRuntimePermission
import kotlinx.android.synthetic.main.layout_vision.*
import java.io.IOException

class BarcodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)

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

                readBarcode(bitmap)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /* Read barcode from an image */
    private fun readBarcode(bitmap: Bitmap) {

        /**
         * An image object that is created from either a bitmap, media.Image, ByteBuffer, Byte array or file on the
         * device which will be used for on device and cloud API detectors
         */
        val vision = FirebaseVisionImage.fromBitmap(bitmap)
        ivSelectedImage.setImageBitmap(bitmap)

        /** Create barcode detector on the instance of firebase vision */
        val barcodeReader = FirebaseVision.getInstance().visionBarcodeDetector

        /** Pass firebase vision image to detector's detectInImage method */
        barcodeReader.detectInImage(vision).addOnCompleteListener {
            if (it.isSuccessful) {
                val builder = StringBuilder()

                it.result?.let { visionBarcode ->
                    for (barcode in visionBarcode) {
                        builder.append(barcode.displayValue)
                    }
                }

                tvExtractedText.text = builder.toString()
            } else {
                Toast.makeText(
                    this@BarcodeActivity,
                    getString(R.string.toast_un_able_to_read_bar_code),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}