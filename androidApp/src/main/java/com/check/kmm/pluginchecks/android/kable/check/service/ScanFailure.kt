package com.check.kmm.pluginchecks.android.kable.check.service

sealed class ScanFailure(val cause: Throwable? = null) {
     data class BluetoothNotEnabled(val aCause: Throwable? = null) : ScanFailure(aCause)
     class PermissionsMissing(val aCause: Throwable? = null) : ScanFailure(aCause)
     data class OtherFailure(val message: CharSequence,val aCause: Throwable? = null) : ScanFailure(aCause)
}
