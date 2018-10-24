package devfest.hyd.mlkitcodelab.utils

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import devfest.hyd.mlkitcodelab.PermissionWithHandledDeniedForever
import devfest.hyd.mlkitcodelab.askPermissions

private const val IMAGE_PICKER_TYPE = "image/*"
private const val SELECT_IMAGE = "Select Picture"

fun AppCompatActivity.askRuntimePermission(permissionArray: Array<String>) {

    supportFragmentManager?.askPermissions(permissionArray, object :
        PermissionWithHandledDeniedForever {
        override fun onPermissionGranted() {
            launchImagePicker()
        }

        override fun onPermissionDenied(deniedPermissionList: ArrayList<String>) {
            Toast.makeText(
                this@askRuntimePermission,
                "Permission required to select image from gallery",
                Toast.LENGTH_SHORT
            ).show()
        }
    })
}

/** Open image picker dialog to select image from the device */
private fun Activity.launchImagePicker() {
    val intent = Intent()
    // Show only images, no videos or anything else
    intent.type = IMAGE_PICKER_TYPE
    intent.action = Intent.ACTION_GET_CONTENT
    // Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, SELECT_IMAGE), 101)
}
