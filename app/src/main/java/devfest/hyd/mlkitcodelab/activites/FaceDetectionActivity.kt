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
import java.lang.StringBuilder
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions



class FaceDetectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)

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

                detectFaceFromImage(bitmap)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /* Detect face from an image */
    private fun detectFaceFromImage(bitmap: Bitmap) {
        /**
         * An image object that is created from either a bitmap, media.Image, ByteBuffer, Byte array or file on the
         * device which will be used for on device and cloud API detectors
         */
        val vision = FirebaseVisionImage.fromBitmap(bitmap)
        ivSelectedImage.setImageBitmap(bitmap)

        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        /** Create face detector on the instance of firebase vision */
        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        /** Pass firebase vision image to detector's detectInImage method */
        faceDetector.detectInImage(vision)
            .addOnCompleteListener {
                val stringBuilder = StringBuilder()

                if (it.isSuccessful) {
                    it.result?.let { visionFace ->
                        for (face in visionFace) {
                            stringBuilder.append("Smile Probability: ").append(face.smilingProbability).append("\n")
                        }
                        tvExtractedText.text = stringBuilder.toString()
                    }
                } else {
                    Toast.makeText(
                        this@FaceDetectionActivity,
                        getString(R.string.toast_un_able_to_detect_face),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}