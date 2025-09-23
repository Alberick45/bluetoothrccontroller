package com.robocar.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robocar.controller.bluetooth.BluetoothManager
import com.robocar.controller.bluetooth.BluetoothService
import com.robocar.controller.ui.theme.RoboCarControllerTheme
import com.robocar.controller.ui.components.CustomJoystick

class MainActivity : ComponentActivity() {
    
    private var bluetoothService: BluetoothService? = null
    private var bluetoothManager: BluetoothManager? = null
    
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
        override fun onDeviceFound(device: android.bluetooth.BluetoothDevice) {}
        override fun onDiscoveryFinished() {}
        
        override fun onConnectionStateChanged(isConnected: Boolean) {
            if (!isConnected) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Bluetooth disconnected", Toast.LENGTH_SHORT).show()
                    // Return to device scan activity
                    startActivity(Intent(this@MainActivity, DeviceScanActivity::class.java))
                    finish()
                }
            }
        }
        
        override fun onDataSent(command: String) {
            // This will be handled in the UI
        }
        
        override fun onError(message: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bind to Bluetooth service
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        setContent {
            RoboCarControllerTheme {
                MainControlScreen()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
    
    @Composable
    fun MainControlScreen() {
        var commandLog by remember { mutableStateOf<List<String>>(emptyList()) }
        var useJoystick by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        // Function to send command and update log
        fun sendCommand(command: String) {
            bluetoothManager?.sendCommand(command)
            commandLog = (listOf("${System.currentTimeMillis()}: $command") + commandLog).take(50)
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
                // Header with disconnect button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RoboCar Controller",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        // Toggle between joystick and buttons
                        IconButton(
                            onClick = { useJoystick = !useJoystick }
                        ) {
                            Icon(
                                imageVector = if (useJoystick) Icons.Default.Games else Icons.Default.TouchApp,
                                contentDescription = if (useJoystick) "Switch to Buttons" else "Switch to Joystick"
                            )
                        }
                        
                        // Disconnect button
                        IconButton(
                            onClick = {
                                bluetoothManager?.disconnect()
                                context.startActivity(Intent(context, DeviceScanActivity::class.java))
                                (context as? ComponentActivity)?.finish()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = "Disconnect"
                            )
                        }
                    }
                }
                
                // Main content area - optimized for landscape
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left side - Movement controls
                    Column(
                        modifier = Modifier.weight(1.5f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (useJoystick) {
                            JoystickControlSection(onCommand = ::sendCommand)
                        } else {
                            ButtonControlSection(onCommand = ::sendCommand)
                        }
                    }
                    
                    // Middle - Action and Lift controls
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Action buttons (Pick/Drop)
                        ActionButtonsSection(onCommand = ::sendCommand)
                        
                        // Lift controls
                        LiftControlsSection(onCommand = ::sendCommand)
                    }
                    
                    // Right side - Command log
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Command Log",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            LazyColumn {
                                items(commandLog) { command ->
                                    Text(
                                        text = command,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun ButtonControlSection(onCommand: (String) -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Movement Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // D-pad layout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Forward button
                    ControlButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "Forward",
                        color = Color(0xFF4CAF50),
                        onClick = { onCommand("F") }
                    )
                    
                    // Left, Stop, Right row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControlButton(
                            icon = Icons.Default.KeyboardArrowLeft,
                            label = "Left",
                            color = Color(0xFF2196F3),
                            onClick = { onCommand("L") }
                        )
                        
                        ControlButton(
                            icon = Icons.Default.Stop,
                            label = "Stop",
                            color = Color(0xFFF44336),
                            onClick = { onCommand("S") }
                        )
                        
                        ControlButton(
                            icon = Icons.Default.KeyboardArrowRight,
                            label = "Right",
                            color = Color(0xFF2196F3),
                            onClick = { onCommand("R") }
                        )
                    }
                    
                    // Backward button
                    ControlButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Backward",
                        color = Color(0xFFFF5722),
                        onClick = { onCommand("B") }
                    )
                }
            }
        }
    }
    
    @Composable
    fun JoystickControlSection(onCommand: (String) -> Unit) {
        var lastCommand by remember { mutableStateOf("") }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Joystick Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                CustomJoystick(
                    modifier = Modifier.size(180.dp),
                    onMove = { angle, strength ->
                        val command = when {
                            strength < 30 -> "S" // Stop when joystick is near center
                            angle in 315..360 || angle in 0..45 -> "R" // Right
                            angle in 45..135 -> "F" // Forward
                            angle in 135..225 -> "L" // Left
                            angle in 225..315 -> "B" // Backward
                            else -> "S"
                        }
                        
                        if (command != lastCommand) {
                            lastCommand = command
                            onCommand(command)
                        }
                    }
                )
            }
        }
    }
    
    @Composable
    fun ActionButtonsSection(onCommand: (String) -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Action Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(
                        icon = Icons.Default.Add,
                        label = "Pick",
                        color = Color(0xFFFF9800),
                        onClick = { onCommand("P") }
                    )
                    
                    ControlButton(
                        icon = Icons.Default.GetApp,
                        label = "Drop",
                        color = Color(0xFF795548),
                        onClick = { onCommand("D") }
                    )
                }
            }
        }
    }
    
    @Composable
    fun LiftControlsSection(onCommand: (String) -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Lift Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("L1", "L2", "L3", "L4").forEach { level ->
                        ControlButton(
                            icon = Icons.Default.VerticalAlignTop,
                            label = level,
                            color = Color(0xFF9C27B0),
                            onClick = { onCommand(level) }
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun ControlButton(
        icon: ImageVector,
        label: String,
        color: Color,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White
            ),
            shape = CircleShape
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}