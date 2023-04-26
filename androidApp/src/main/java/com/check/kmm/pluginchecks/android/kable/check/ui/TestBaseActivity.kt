package com.check.kmm.pluginchecks.android.kable.check.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.check.kmm.pluginchecks.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

@SuppressLint("MissingPermission")
abstract class TestBaseActivity : AppCompatActivity() {

    var testedException: Throwable = NotThrownException()

    val _testSubjectFlow = MutableStateFlow(TestDataContainer())

    val testSubjectFlow: StateFlow<TestDataContainer> = _testSubjectFlow


    var currentTestDataContainer: TestDataContainer = TestDataContainer()
        set(value) {
            _testSubjectFlow.value = value
            field = value
        }

    fun prepareClickAction(@IdRes viewId: Int, onClick: () -> Unit) {
        findViewById<View>(viewId).setOnClickListener {
            onClick()
        }
    }

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

}