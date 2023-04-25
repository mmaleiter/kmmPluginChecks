package com.check.kmm.pluginchecks.android.kable.check.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.check.kmm.pluginchecks.android.kable.check.data.AdvertisementWrapper
import com.check.kmm.pluginchecks.android.kable.check.data.Device
import com.check.kmm.pluginchecks.android.kable.check.service.ConnectState
import com.check.kmm.pluginchecks.android.kable.check.service.ScanFailure
import com.check.kmm.pluginchecks.android.kable.check.service.ScanStatus
import com.check.kmm.pluginchecks.android.kable.check.util.cancelChildren
import com.check.kmm.pluginchecks.android.kable.check.util.childScope
import com.juul.kable.Bluetooth
import com.juul.kable.Scanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(15)


@HiltViewModel
class BleViewModel @Inject constructor(): ViewModel() {

    private val _job = SupervisorJob()
    private val _scope = CoroutineScope(Dispatchers.IO + _job)
    private val _scanScope = _scope
    private val _connectScope = _scope.childScope()

    private val _scanner = Scanner()
    private val _blueKable = Bluetooth


    private val _foundDevices = hashMapOf<String, AdvertisementWrapper>()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements =
        MutableStateFlow<List<AdvertisementWrapper>>(emptyList())
    val advertisements = _advertisements.asStateFlow()

//    val discoveredServices = bluetoothLeServiceWrapper.connectedDeviceServices

    private val _onConnectEventFlow = MutableSharedFlow<Unit>()
    // When a Unit is delivered on this flow, it indicates that connect() was called
    val onConnectEventFlow = _onConnectEventFlow.asSharedFlow()

    private val _connectState = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val connectState = _connectState.asStateFlow()


    private val _activeDevice = MutableStateFlow<Device?>(null)
    val activeDevice = _activeDevice.asStateFlow()

    fun toggleScan() {
        when (scanStatus.value) {
            is ScanStatus.Running -> stopScan()
            is ScanStatus.Idle -> startScan()
            is ScanStatus.Failed -> {
            }
        }
    }

    fun startScan() {
        disconnect()
        when {
            _scanStatus.value == ScanStatus.Running -> return

//            !isBluetoothEnabled() -> _scanStatus.value =
//                ScanStatus.Failed(ScanFailure.BluetoothNotEnabled)
            //TODO
//            !hasLocationAndConnectPermissions -> _scanStatus.value =
//                ScanStatus.Failed(ScanFailure.PermissionsMissing)
            else -> {
                _scanStatus.value = ScanStatus.Running

                _scanScope.launch {
                    withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                        _scanner
                            .advertisements
                            .catch { cause ->
                                _scanStatus.value =
                                    ScanStatus.Failed(
                                        ScanFailure.OtherFailure(
                                            cause.message ?: "Unknown error"
                                        )
                                    )
                            }
                            .collect { advertisement ->
                                _foundDevices[advertisement.address] =
                                    AdvertisementWrapper(advertisement)
                                _advertisements.value = _foundDevices.values.toList()
                                Log.e("APP", advertisement.toString())
                            }
                    }
                }.invokeOnCompletion {
                    Log.e("APP", "SCAN IS STOPPING")
                    _scanStatus.value = ScanStatus.Idle
                }
            }
        }


    }

    fun stopScan() {
        _scanScope.cancelChildren()
    }

    fun disconnect() {
        _connectScope.cancelChildren()
        _connectState.value = ConnectState.Idle

        // TODO:
        runBlocking {
            _activeDevice.value?.disconnect()
            _activeDevice.value = null
        }
    }

    fun connect(advertisement: AdvertisementWrapper) {
        viewModelScope.launch {
            _onConnectEventFlow.emit(Unit)
        }
    }
}