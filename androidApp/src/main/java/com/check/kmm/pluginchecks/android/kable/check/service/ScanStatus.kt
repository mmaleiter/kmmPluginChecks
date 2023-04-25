package com.check.kmm.pluginchecks.android.kable.check.service

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Running : ScanStatus()
    data class Failed(val failure: ScanFailure) : ScanStatus()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Running -> "Running"
            else -> "Failed"
        }
    }
}