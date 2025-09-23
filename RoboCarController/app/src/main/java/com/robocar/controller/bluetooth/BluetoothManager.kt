package com.robocar.controller.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothManager"
        private val HC_05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        
        @Volatile
        private var INSTANCE: BluetoothManager? = null
        
        fun getInstance(context: Context): BluetoothManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BluetoothManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    interface BluetoothCallback {
        fun onDeviceFound(device: BluetoothDevice)
        fun onDiscoveryFinished()
        fun onConnectionStateChanged(isConnected: Boolean)
        fun onDataSent(command: String)
        fun onError(message: String)
    }
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    
    private var callback: BluetoothCallback? = null
    
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { callback?.onDeviceFound(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    callback?.onDiscoveryFinished()
                }
            }
        }
    }
    
    fun setCallback(callback: BluetoothCallback) {
        this.callback = callback
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    fun startDiscovery(): Boolean {
        if (!hasBluetoothPermissions()) {
            callback?.onError("Bluetooth permissions not granted")
            return false
        }
        
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        // Register for broadcasts when a device is discovered
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)
        
        return bluetoothAdapter?.startDiscovery() ?: false
    }
    
    fun stopDiscovery() {
        try {
            context.unregisterReceiver(discoveryReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
        
        if (hasBluetoothPermissions()) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }
    
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): Set<BluetoothDevice> {
        return if (hasBluetoothPermissions()) {
            bluetoothAdapter?.bondedDevices ?: emptySet()
        } else {
            emptySet()
        }
    }
    
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        coroutineScope.launch {
            try {
                disconnect() // Ensure any existing connection is closed
                
                withContext(Dispatchers.IO) {
                    // Cancel discovery as it slows down connection
                    if (hasBluetoothPermissions()) {
                        bluetoothAdapter?.cancelDiscovery()
                    }
                    
                    // Wait a moment for discovery to fully cancel
                    Thread.sleep(500)
                    
                    var connected = false
                    var lastException: Exception? = null
                    
                    // Strategy 1: Standard UUID connection
                    if (!connected) {
                        try {
                            Log.d(TAG, "Trying standard HC-05 UUID connection...")
                            bluetoothSocket = device.createRfcommSocketToServiceRecord(HC_05_UUID)
                            bluetoothSocket?.connect()
                            connected = bluetoothSocket?.isConnected == true
                            Log.d(TAG, "Standard connection successful")
                        } catch (e: Exception) {
                            Log.w(TAG, "Standard UUID connection failed: ${e.message}")
                            lastException = e
                            try {
                                bluetoothSocket?.close()
                            } catch (closeEx: IOException) { /* ignore */ }
                        }
                    }
                    
                    // Strategy 2: Insecure RFCOMM connection
                    if (!connected) {
                        try {
                            Log.d(TAG, "Trying insecure RFCOMM connection...")
                            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(HC_05_UUID)
                            bluetoothSocket?.connect()
                            connected = bluetoothSocket?.isConnected == true
                            Log.d(TAG, "Insecure connection successful")
                        } catch (e: Exception) {
                            Log.w(TAG, "Insecure connection failed: ${e.message}")
                            lastException = e
                            try {
                                bluetoothSocket?.close()
                            } catch (closeEx: IOException) { /* ignore */ }
                        }
                    }
                    
                    // Strategy 3: Fallback reflection method (channel 1)
                    if (!connected) {
                        try {
                            Log.d(TAG, "Trying reflection method (channel 1)...")
                            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                            bluetoothSocket = method.invoke(device, 1) as BluetoothSocket
                            bluetoothSocket?.connect()
                            connected = bluetoothSocket?.isConnected == true
                            Log.d(TAG, "Reflection method successful")
                        } catch (e: Exception) {
                            Log.w(TAG, "Reflection method failed: ${e.message}")
                            lastException = e
                            try {
                                bluetoothSocket?.close()
                            } catch (closeEx: IOException) { /* ignore */ }
                        }
                    }
                    
                    // Strategy 4: Try different channels (common for HC-05)
                    val channels = arrayOf(2, 3, 4, 5)
                    for (channel in channels) {
                        if (!connected) {
                            try {
                                Log.d(TAG, "Trying reflection method (channel $channel)...")
                                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                                bluetoothSocket = method.invoke(device, channel) as BluetoothSocket
                                bluetoothSocket?.connect()
                                connected = bluetoothSocket?.isConnected == true
                                if (connected) {
                                    Log.d(TAG, "Connected successfully on channel $channel")
                                    break
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Channel $channel failed: ${e.message}")
                                lastException = e
                                try {
                                    bluetoothSocket?.close()
                                } catch (closeEx: IOException) { /* ignore */ }
                            }
                        }
                    }
                    
                    if (!connected) {
                        throw lastException ?: IOException("All connection strategies failed")
                    }
                    
                    // Verify connection and setup output stream
                    outputStream = bluetoothSocket?.outputStream
                    if (outputStream == null) {
                        throw IOException("Failed to get output stream")
                    }
                    
                    // Test the connection by sending a test command
                    try {
                        outputStream?.write("TEST".toByteArray())
                        outputStream?.flush()
                        Log.d(TAG, "Connection test successful")
                    } catch (e: IOException) {
                        Log.w(TAG, "Connection test failed, but proceeding anyway")
                    }
                }
                
                isConnected = true
                callback?.onConnectionStateChanged(true)
                Log.d(TAG, "Successfully connected to ${device.name}")
                
            } catch (e: Exception) {
                Log.e(TAG, "All connection attempts failed", e)
                isConnected = false
                callback?.onConnectionStateChanged(false)
                
                val errorMsg = when {
                    e.message?.contains("Service discovery failed") == true -> 
                        "Device not found. Make sure ${device.name} is powered on and in range."
                    e.message?.contains("Connection refused") == true -> 
                        "Connection refused by ${device.name}. Try unpairing and re-pairing the device."
                    e.message?.contains("timeout") == true -> 
                        "Connection timeout to ${device.name}. Move closer to the device and try again."
                    else -> 
                        "Failed to connect to ${device.name}. Ensure it's powered on, unpaired from other devices, and try again."
                }
                
                callback?.onError(errorMsg)
                
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    Log.e(TAG, "Could not close the client socket", closeException)
                }
            }
        }
    }
    
    fun disconnect() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    outputStream?.close()
                    bluetoothSocket?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing connection", e)
                }
            }
            
            isConnected = false
            outputStream = null
            bluetoothSocket = null
            callback?.onConnectionStateChanged(false)
        }
    }
    
    fun sendCommand(command: String): Boolean {
        if (!isConnected || outputStream == null) {
            callback?.onError("Not connected to device")
            return false
        }
        
        coroutineScope.launch {
            var retryCount = 0
            val maxRetries = 2
            var success = false
            
            while (retryCount <= maxRetries && !success) {
                try {
                    withContext(Dispatchers.IO) {
                        // Check if socket is still connected before sending
                        if (bluetoothSocket?.isConnected == false) {
                            throw IOException("Socket is not connected")
                        }
                        
                        outputStream?.write(command.toByteArray())
                        outputStream?.flush()
                    }
                    
                    callback?.onDataSent(command)
                    Log.d(TAG, "Sent command: $command")
                    success = true
                    
                } catch (e: IOException) {
                    retryCount++
                    Log.w(TAG, "Command send attempt $retryCount failed: ${e.message}")
                    
                    if (retryCount <= maxRetries) {
                        // Short delay before retry
                        try {
                            kotlinx.coroutines.delay(100)
                        } catch (ex: Exception) { /* ignore */ }
                        
                        // Try to re-establish output stream if possible
                        try {
                            withContext(Dispatchers.IO) {
                                if (bluetoothSocket?.isConnected == true) {
                                    outputStream = bluetoothSocket?.outputStream
                                }
                            }
                        } catch (streamEx: IOException) {
                            Log.w(TAG, "Failed to re-establish output stream")
                        }
                    } else {
                        // Only disconnect after all retries failed
                        Log.e(TAG, "All retries failed for command: $command")
                        callback?.onError("Connection unstable. Please reconnect.")
                        disconnect()
                    }
                }
            }
        }
        return true
    }
    
    fun isConnected(): Boolean {
        return isConnected && bluetoothSocket?.isConnected == true
    }
    
    private fun checkConnectionHealth(): Boolean {
        return try {
            bluetoothSocket?.isConnected == true && outputStream != null
        } catch (e: Exception) {
            false
        }
    }
    
    @SuppressLint("MissingPermission")
    fun getDeviceInfo(device: BluetoothDevice): String {
        return try {
            "Device: ${device.name ?: "Unknown"} (${device.address})\n" +
            "Bond State: ${when(device.bondState) {
                BluetoothDevice.BOND_BONDED -> "Paired"
                BluetoothDevice.BOND_BONDING -> "Pairing..."
                BluetoothDevice.BOND_NONE -> "Not Paired"
                else -> "Unknown"
            }}\n" +
            "Type: ${when(device.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
                BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
                else -> "Unknown"
            }}"
        } catch (e: Exception) {
            "Device info unavailable: ${e.message}"
        }
    }
    
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun cleanup() {
        stopDiscovery()
        disconnect()
        callback = null
    }
}