package com.check.kmm.pluginchecks.android.kable.check.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.check.kmm.pluginchecks.android.R
import com.check.kmm.pluginchecks.android.databinding.ActivityStartBinding
import com.check.kmm.pluginchecks.android.kable.check.service.BluetoothLeService
import com.check.kmm.pluginchecks.android.kable.check.service.ScanStatus
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.FieldPosition

class StartActivity : AppCompatActivity() {

    private lateinit var activityResultHandler: ActivityResultHandler

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityStartBinding

    private lateinit var bluetoothService: BluetoothLeService
    private var mBound: Boolean = false

    var testedException: Throwable = NotThrownException()

    private var currentTestDataContainer: TestDataContainer = TestDataContainer()
        set(value) {
            _testSubjectFlow.value = value
            field = value
        }

    private val _testSubjectFlow = MutableStateFlow(TestDataContainer())
    val testSubjectFlow: StateFlow<TestDataContainer> = _testSubjectFlow

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as BluetoothLeService.LocalBinder
            bluetoothService = binder.getService()
            mBound = true
            lifecycleScope.launch {
                bluetoothService.scanStatus.map {
                    if (_testSubjectFlow.tryEmit(testSubjectFlow.value.copy(
                            scanStatus = it)
                        )) {
                        Timber.v("As expected! :-)")
                    } else {
                        Timber.e("Suspicious")
                    }
                    it
                }.collect {

                    Timber.v(it.toString())
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothLeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_start)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
        }
        setupButtons()
    }

    fun prepareClickAction(@IdRes viewId: Int, onClick: () -> Unit) {
        findViewById<View>(viewId).setOnClickListener {
            onClick()
        }
    }

    @SuppressLint("MissingPermission")
    fun setBluetooth(enable: Boolean): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable()
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable()
        }
        // No need to change bluetooth state
        return true
    }

    fun showLoader(show: Boolean) {
        Timber.e("Showing loader: -> $show")
        findViewById<View>(R.id.blockingProgress).visibility = if(show) View.VISIBLE else View.GONE

    }

    private fun setupButtons() {
        with(binding.content) {
            startScan.setOnClickListener {
                bluetoothService.startScan()
            }
            stopScan.setOnClickListener {
                bluetoothService.stopScan()
            }
            connect.setOnClickListener {
                bluetoothService.connect(null)
            }
            disconnect.setOnClickListener {
                bluetoothService.disconnect()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_start)
        return navController.navigateUp(appBarConfiguration)
            || super.onSupportNavigateUp()
    }

}

data class TestDataContainer(
    val testStarted: Boolean = false,
    val createdTimeStamp: Long = System.currentTimeMillis(),
    val scanStatus: ScanStatus = ScanStatus.Idle,
) {
}


class NotThrownException : Throwable("Initial or neutral state, like a boolean false.")

interface TestLoad {
    val timeStamp: Long
    val actionSequenceMap: Map<Int, () -> Unit>
    val actionSequenceKeys: List<Int>
}

data class TestPartAction(
    val createdTimeStamp: Long = System.currentTimeMillis(),
    val position: Int = -1,
    val multipleTests: Boolean = false,
    val timeOutMillis: Long = 0,
    val startAction: () -> Unit = {},
    val stopAction: () -> Unit = {},
    val stopBeforeTimeout: Boolean = false,
) {
    val hasTimeOut: Boolean = timeOutMillis > 0

}

sealed class TestState {

}