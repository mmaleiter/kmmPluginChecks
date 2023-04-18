package com.check.kmm.pluginchecks.android.kable.check.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.check.kmm.pluginchecks.android.R
import com.check.kmm.pluginchecks.android.databinding.ActivityStartBinding
import com.check.kmm.pluginchecks.android.kable.check.BluetoothLeService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStartBinding

    private lateinit var bluetoothService: BluetoothLeService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as BluetoothLeService.LocalBinder
            bluetoothService = binder.getService()
            mBound = true
            lifecycleScope.launch {
                bluetoothService.scanStatus.collect{
                    Log.e("ScanStatus", it.toString())
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothLeService::class.java).also {
            intent ->
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

    private fun setupButtons() {
        with(binding.content){
            startScan.setOnClickListener {
                bluetoothService.startScan()
            }
            stopScan.setOnClickListener {
                bluetoothService.stopScan()
            }
            connect.setOnClickListener {  }
            disconnect.setOnClickListener {  }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_start)
        return navController.navigateUp(appBarConfiguration)
            || super.onSupportNavigateUp()
    }
}