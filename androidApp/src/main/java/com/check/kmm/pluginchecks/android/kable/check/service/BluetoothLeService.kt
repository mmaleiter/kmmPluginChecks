package com.check.kmm.pluginchecks.android.kable.check.service

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.check.kmm.pluginchecks.android.kable.check.data.AdvertisementWrapper
import com.check.kmm.pluginchecks.android.kable.check.data.Device
import com.check.kmm.pluginchecks.android.kable.check.ui.TestDataContainer
import com.check.kmm.pluginchecks.android.kable.check.util.cancelChildren
import com.check.kmm.pluginchecks.android.kable.check.util.childScope
import com.juul.kable.Advertisement
import com.juul.kable.Bluetooth
import com.juul.kable.DiscoveredService
import com.juul.kable.Scanner
import com.juul.kable.peripheral

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(15)

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothLeService : LifecycleService() {
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



    private fun isBluetoothEnabled(): Boolean {
        return BluetoothAdapter.getDefaultAdapter().isEnabled
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { intent ->
            actOnIntent(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun actOnIntent(intent: Intent) {
//        when (intent.action) {
//            null -> throw IllegalStateException("actOnIntent(intent: Intent)")
//        }
    }

    private val _bluetoothEnabledJob: Job = _scope.launch {
        _blueKable.availability.collect {
            Log.e("BluetoothAvailable", "Available: $it")
        }
    }

    fun startScan() {
        disconnect()

        when {
            _scanStatus.value == ScanStatus.Running -> return

            !isBluetoothEnabled() -> _scanStatus.value =
                ScanStatus.Failed(ScanFailure.BluetoothNotEnabled)
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

    private val _activeDevice = MutableStateFlow<Device?>(null)
    val activeDevice = _activeDevice.asStateFlow()

    private val _connectState = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val connectState = _connectState.asStateFlow()

    val connectedDeviceServices: StateFlow<List<DiscoveredService>> = _activeDevice.flatMapLatest {
        it?.discoveredServices ?: flowOf(listOf())
    }.stateIn(scope = lifecycleScope, initialValue = listOf(), started = SharingStarted.WhileSubscribed(5000))

    fun disconnect() {
        _connectScope.cancelChildren()
        _connectState.value = ConnectState.Idle

        // TODO:
        runBlocking {
            _activeDevice.value?.disconnect()
            _activeDevice.value = null
        }
    }

    fun connect(advertisement: Advertisement) {
        stopScan()
        disconnect()

        _connectState.value = ConnectState.PeripheralConnecting

        _connectScope.launch {
            val per = _connectScope.peripheral(advertisement)

            val bridge = Device(per)
            _activeDevice.value = bridge

            bridge.connect()
            _connectState.value = ConnectState.PeripheralConnected
        }
    }

    private val _binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BluetoothLeService = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return _binder
    }

    override fun onDestroy() {
        super.onDestroy()
        _job.cancel()
        _bluetoothEnabledJob.cancel()
    }

    companion object {

    }

}

