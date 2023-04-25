package com.check.kmm.pluginchecks.android.kable.check.service

sealed class ConnectState {
    object Idle : ConnectState()
    object PeripheralConnecting : ConnectState()
    object PeripheralConnected : ConnectState()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is PeripheralConnecting -> "Connecting"
            is PeripheralConnected -> "Connected"
        }
    }
}