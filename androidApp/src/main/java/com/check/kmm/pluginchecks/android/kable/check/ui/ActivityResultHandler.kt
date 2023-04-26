package com.check.kmm.pluginchecks.android.kable.check.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ActivityResultHandler(
    private val registry: ActivityResultRegistry
) {

    private val handlers = mutableListOf<ActivityResultLauncher<*>>()

    fun unregisterHandlers() {
        handlers.forEach {
            it.unregister()
        }
    }

    suspend fun requestLocationPermission(): Boolean {
        return suspendCoroutine { continuation ->
            val launcher = registry.register(
                LOCATION_PERMISSION_REQUEST,
//                lifecycleOwner,
                ActivityResultContracts.RequestPermission()
            ) {
                continuation.resumeWith(Result.success(it))
            }
            handlers.add(launcher)
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    suspend fun requestBluetoothActivation(): Boolean {
        return suspendCoroutine { continuation ->
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            val launcher = registry.register(
                BLUETOOTH_ON_REQUEST,
//                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                continuation.resume(
                    result.resultCode == Activity.RESULT_OK
                )
            }
            handlers.add(launcher)
            launcher.launch(enableBtIntent)
        }
    }

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun requestLocationActivation(
        intentSenderRequest: IntentSenderRequest,
    ): Boolean {
        return suspendCoroutine { continuation ->
            val launcher = registry.register(
                LOCATION_ACTIVATION_REQUEST,
//                lifecycleOwner,
                ActivityResultContracts.StartIntentSenderForResult()
            ) {
                continuation.resume(it.resultCode == Activity.RESULT_OK)
            }
            handlers.add(launcher)
            launcher.launch(intentSenderRequest)
        }
    }


//    suspend fun enableLocation(context: Context): Boolean =
//        suspendCoroutine { continuation ->
//
//            val locationSettingsRequest = LocationSettingsRequest.Builder()
////        .setNeedBle(true)
//                .addLocationRequest(
//                    LocationRequest.create().apply {
//                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                    }
//                )
//                .build()
//
//            val client: SettingsClient = LocationServices.getSettingsClient(context)
//            val task: Task<LocationSettingsResponse> =
//                client.checkLocationSettings(locationSettingsRequest)
//
//            task.addOnSuccessListener {
//                continuation.resume(true)
//            }
//            task.addOnFailureListener { exception ->
//                if (exception is ResolvableApiException &&
//                    exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED
//                ) {
//                    val intentSenderRequest =
//                        IntentSenderRequest.Builder(exception.resolution).build()
//
//                    CoroutineScope(continuation.context).launch {
//                        val result = requestLocationActivation(intentSenderRequest)
//                        continuation.resume(result)
//                    }
//                } else {
//                    continuation.resume(false)
//                }
//            }
//        }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST = "LOCATION_REQUEST"
        private const val BLUETOOTH_ON_REQUEST = "LOCATION_REQUEST"
        private const val LOCATION_ACTIVATION_REQUEST = "LOCATION_REQUEST"
    }
}