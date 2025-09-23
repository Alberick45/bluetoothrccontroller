package com.robocar.controller.bluetooth

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BluetoothService : Service() {
    
    private val binder = LocalBinder()
    private lateinit var bluetoothManager: BluetoothManager
    
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }
    
    override fun onCreate() {
        super.onCreate()
        bluetoothManager = BluetoothManager.getInstance(this)
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    fun getBluetoothManager(): BluetoothManager {
        return bluetoothManager
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }
}