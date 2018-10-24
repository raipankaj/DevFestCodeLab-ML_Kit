package devfest.hyd.mlkitcodelab.activites

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import devfest.hyd.mlkitcodelab.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btFaceDetect.setOnClickListener {
            startActivity(Intent(this@MainActivity, FaceDetectionActivity::class.java))
        }

        btBarcodeScan.setOnClickListener {
            startActivity(Intent(this@MainActivity, BarcodeActivity::class.java))
        }

        btTextRec.setOnClickListener {
            startActivity(Intent(this@MainActivity, TextRecognitionActivity::class.java))
        }
    }

}
