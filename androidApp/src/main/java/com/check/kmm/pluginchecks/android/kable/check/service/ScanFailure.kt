package com.check.kmm.pluginchecks.android.kable.check.service

sealed class ScanFailure {
    object BluetoothNotEnabled : ScanFailure()
    object PermissionsMissing : ScanFailure()
    data class OtherFailure(val message: CharSequence) : ScanFailure()
}
