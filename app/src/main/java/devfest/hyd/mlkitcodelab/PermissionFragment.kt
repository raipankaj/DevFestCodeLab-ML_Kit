package devfest.hyd.mlkitcodelab

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager

class PermissionFragment : Fragment() {
    companion object {

        private const val PERMISSION_LIST = "permission_list"
        private const val REQUEST_CODE = 101
        private var mPermissionCallback: Any? = null

        @JvmStatic
        fun instanceOf(
            permissionList: Array<String>,
            permissionCallback: Any
        ): PermissionFragment {

            mPermissionCallback = permissionCallback
            return PermissionFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(PERMISSION_LIST, permissionList)
                }
            }
        }
    }

    private lateinit var mRequestedPermissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(PermissionLifecycle())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val deniedPermissionList: ArrayList<String> = ArrayList()

        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty()) {

            addDeniedPermissions(grantResults, deniedPermissionList, permissions)
            mPermissionCallback?.also {
                if (deniedPermissionList.isEmpty()) {
                    removeCurrentFragment()
                    when (it) {
                        is PermissionWithHandledDeniedForever -> it.onPermissionGranted()
                        is PermissionCallback -> it.onPermissionGranted()
                    }
                } else {
                    val deniedForeverList: ArrayList<String> =
                        addForeverDeniedPermissions(grantResults, permissions)

                    if (deniedForeverList.isNotEmpty()) {
                        when (it) {
                            is PermissionWithHandledDeniedForever -> {
                                AlertDialog.Builder(activity)
                                    .setMessage("Please enable all permissions from settings to proceed further.")
                                    .setPositiveButton("Ok") { _, _ ->
                                        removeCurrentFragment()
                                        activity?.openSettings()
                                    }
                                    .setNegativeButton("Cancel") { _, _ ->
                                    }.show()

                            }
                            is PermissionCallback -> {
                                it.onPermissionDeniedForever()
                            }
                        }
                    } else {
                        removeCurrentFragment()
                        when (it) {
                            is PermissionWithHandledDeniedForever -> it.onPermissionDenied(
                                deniedPermissionList
                            )
                            is PermissionCallback -> it.onPermissionDenied(deniedPermissionList)
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addForeverDeniedPermissions(
        grantResults: IntArray,
        permissions: Array<out String>
    ): ArrayList<String> {
        val deniedForeverList: ArrayList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (counter in 0 until grantResults.size) {
                val permission = permissions[counter]
                if (grantResults[counter] == PackageManager.PERMISSION_DENIED
                    && activity?.shouldShowRequestPermissionRationale(permission) != true
                ) {
                    deniedForeverList.add(permission)
                }
            }
        }

        return deniedForeverList
    }

    /**
     * Add the denied permission in an array list to keep track of denied permissions.
     */
    private fun addDeniedPermissions(
        grantResults: IntArray, deniedPermissionList: ArrayList<String>,
        permissions: Array<out String>
    ) {
        (0 until grantResults.size)
            .filter { grantResults[it] == PackageManager.PERMISSION_DENIED }
            .mapTo(deniedPermissionList) { permissions[it] }
    }

    private fun removeCurrentFragment() {
        fragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun Activity.openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private inner class PermissionLifecycle : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreateEvent() {
            if (mPermissionCallback != null) {
                activity?.let {
                    getRequestedPermissions()
                    askForPermissions(it)
                }
            }
        }

        /**
         * Remove attached callback whenever fragment is about to destroy.
         */
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyEvent() {
            mPermissionCallback = null
        }

        /**
         * Get the list of requested permission as an array string from arguments.
         */
        private fun getRequestedPermissions() {
            arguments?.apply {
                mRequestedPermissions = getStringArray(PERMISSION_LIST)
            }
        }

        /**
         * Find the list of granted permission, if all permission are granted than pass callback method
         * onPermissionGranted.
         *
         * Request for all the permissions that are not granted.
         */
        private fun askForPermissions(it: FragmentActivity) {

            val permissionGrantedList: ArrayList<String> = ArrayList()

            //Find the list of granted permission and add to the array list.
            mRequestedPermissions
                .filter { permission ->
                    (ContextCompat.checkSelfPermission(it, permission)
                            == PackageManager.PERMISSION_GRANTED)
                }
                .mapTo(permissionGrantedList) { it }

            //If all permission are already granted call onPermissionGranted
            if (permissionGrantedList.size == mRequestedPermissions.size) {
                mPermissionCallback?.let {
                    when (it) {
                        is PermissionWithHandledDeniedForever -> it.onPermissionGranted()
                        is PermissionCallback -> it.onPermissionGranted()
                    }
                }
            } else {
                //Requesting for non-granted permissions
                val requestPermissionList: ArrayList<String> = ArrayList()
                mRequestedPermissions
                    .filter { permission ->
                        (ContextCompat.checkSelfPermission(it, permission)
                                != PackageManager.PERMISSION_GRANTED)
                    }
                    .forEach { requestPermissionList.add(it) }

                requestPermissions(requestPermissionList.toTypedArray(), REQUEST_CODE)
            }
        }
    }
}


fun FragmentManager.askPermissions(permissionRequired: Array<String>, permissionCallback: Any) {
    val per = PermissionFragment.instanceOf(permissionRequired, permissionCallback)
    with(beginTransaction()) {
        add(per, "permission_alert")
        commit()
    }
}

interface PermissionWithHandledDeniedForever {
    fun onPermissionGranted()
    fun onPermissionDenied(deniedPermissionList: ArrayList<String>)
}

interface PermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(deniedPermissionList: ArrayList<String>)
    fun onPermissionDeniedForever()
}
