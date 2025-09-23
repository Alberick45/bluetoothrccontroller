package com.robocar.controller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.robocar.controller.bluetooth.BluetoothManager
import com.robocar.controller.bluetooth.BluetoothService
import com.robocar.controller.ui.theme.RoboCarControllerTheme

class DeviceScanActivity : ComponentActivity() {
    
    companion object {
        private const val PREFS_NAME = "bluetooth_prefs"
        private const val LAST_DEVICE_ADDRESS = "last_device_address"
        private const val LAST_DEVICE_NAME = "last_device_name"
        
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    private var bluetoothService: BluetoothService? = null
    private var bluetoothManager: BluetoothManager? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            bluetoothManager = bluetoothService?.getBluetoothManager()
            bluetoothManager?.setCallback(bluetoothCallback)
        }
        
        override fun onServiceDisconnected(name: ComponentName) {
            bluetoothService = null
            bluetoothManager = null
        }
    }
    
    private val bluetoothCallback = object : BluetoothManager.BluetoothCallback {
        override fun onDeviceFound(device: BluetoothDevice) {
            // This will be handled in Compose state
        }
        
        override fun onDiscoveryFinished() {
            // This will be handled in Compose state
        }
        
        override fun onConnectionStateChanged(isConnected: Boolean) {
            if (isConnected) {
                // Save last connected device
                saveLastConnectedDevice()
                // Navigate to main activity
                startActivity(Intent(this@DeviceScanActivity, MainActivity::class.java))
                finish()
            }
        }
        
        override fun onDataSent(command: String) {
            // Not used in this activity
        }
        
        override fun onError(message: String) {
            runOnUiThread {
                Toast.makeText(this@DeviceScanActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkBluetoothEnabled()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
        }
    }
    
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth enabled, continue with setup
        } else {
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Bind to Bluetooth service
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        setContent {
            RoboCarControllerTheme {
                DeviceScanScreen()
            }
        }
        
        // Check permissions and Bluetooth state
        checkPermissions()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager?.stopDiscovery()
        unbindService(serviceConnection)
    }
    
    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            checkBluetoothEnabled()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    private fun checkBluetoothEnabled() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun saveLastConnectedDevice() {
        bluetoothManager?.let { manager ->
            // You would need to track the currently connecting device
            // For simplicity, we'll implement this in the connection method
        }
    }
    
    @Composable
    fun DeviceScanScreen() {
        var isScanning by remember { mutableStateOf(false) }
        var discoveredDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
        var pairedDevices by remember { mutableStateOf<Set<BluetoothDevice>>(emptySet()) }
        var isConnecting by remember { mutableStateOf(false) }
        
        val context = LocalContext.current
        
        // Load paired devices when component is created
        LaunchedEffect(bluetoothManager) {
            bluetoothManager?.let {
                pairedDevices = it.getPairedDevices()
            }
        }
        
        // Set up device discovery callback
        LaunchedEffect(bluetoothManager) {
            bluetoothManager?.setCallback(object : BluetoothManager.BluetoothCallback {
                override fun onDeviceFound(device: BluetoothDevice) {
                    if (!discoveredDevices.contains(device)) {
                        discoveredDevices = discoveredDevices + device
                    }
                }
                
                override fun onDiscoveryFinished() {
                    isScanning = false
                }
                
                override fun onConnectionStateChanged(isConnected: Boolean) {
                    isConnecting = false
                    if (isConnected) {
                        Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                }
                
                override fun onDataSent(command: String) {}
                
                override fun onError(message: String) {
                    isConnecting = false
                    Toast.makeText(context, "Connection failed: $message", Toast.LENGTH_LONG).show()
                }
            })
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Bluetooth Device",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = {
                            if (!isScanning) {
                                discoveredDevices = emptyList()
                                isScanning = true
                                bluetoothManager?.startDiscovery()
                            }
                        },
                        enabled = !isScanning && !isConnecting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan for devices"
                        )
                    }
                }
                
                // Quick connect to last device
                val lastDeviceAddress = sharedPreferences.getString(LAST_DEVICE_ADDRESS, null)
                val lastDeviceName = sharedPreferences.getString(LAST_DEVICE_NAME, null)
                
                if (lastDeviceAddress != null && lastDeviceName != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Quick Connect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Last connected: $lastDeviceName",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val device = pairedDevices.find { it.address == lastDeviceAddress }
                                    device?.let {
                                        isConnecting = true
                                        bluetoothManager?.connectToDevice(it)
                                    }
                                },
                                enabled = !isConnecting && !isScanning
                            ) {
                                if (isConnecting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Connect to $lastDeviceName")
                            }
                        }
                    }
                }
                
                // Scanning indicator
                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning for devices...")
                    }
                }
                
                // Device lists
                LazyColumn {
                    // Paired devices section
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Paired Devices",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(pairedDevices.toList()) { device ->
                            DeviceItem(
                                device = device,
                                isPaired = true,
                                isConnecting = isConnecting,
                                onConnect = {
                                    isConnecting = true
                                    bluetoothManager?.connectToDevice(device)
                                    sharedPreferences.edit()
                                        .putString(LAST_DEVICE_ADDRESS, device.address)
                                        .putString(LAST_DEVICE_NAME, device.name ?: "Unknown Device")
                                        .apply()
                                }
                            )
                        }
                    }
                    
                    // Discovered devices section
                    if (discoveredDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Available Devices",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(discoveredDevices.filter { !pairedDevices.contains(it) }) { device ->
                            DeviceItem(
                                device = device,
                                isPaired = false,
                                isConnecting = isConnecting,
                                onConnect = {
                                    isConnecting = true
                                    bluetoothManager?.connectToDevice(device)
                                    sharedPreferences.edit()
                                        .putString(LAST_DEVICE_ADDRESS, device.address)
                                        .putString(LAST_DEVICE_NAME, device.name ?: "Unknown Device")
                                        .apply()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceItem(
        device: BluetoothDevice,
        isPaired: Boolean,
        isConnecting: Boolean,
        onConnect: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = device.name ?: "Unknown Device",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPaired) {
                        Text(
                            text = "Paired",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Button(
                    onClick = onConnect,
                    enabled = !isConnecting
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connect")
                    }
                }
            }
        }
    }
}