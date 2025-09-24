# 🚗 RoboCar Controller

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Bluetooth](https://img.shields.io/badge/Connectivity-Bluetooth-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**A powerful Android app for controlling RC cars via Bluetooth HC-05 modules**

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Development](#-development) • [Troubleshooting](#-troubleshooting)

</div>

---

## 📋 Overview

RoboCar Controller is a modern Android application built with **Kotlin** and **Jetpack Compose** that provides intuitive remote control for RC cars through Bluetooth connectivity. Designed specifically for **HC-05 Bluetooth modules**, it offers both traditional D-pad controls and modern joystick interface with advanced features like lift controls and command logging.

### 🎯 Target Use Cases
- **RC Car Control**: Full directional movement (Forward, Backward, Left, Right, Stop)
- **Robotic Arms**: Pick/Drop actions with multiple lift positions
- **Educational Projects**: Arduino-based robotics with Bluetooth communication
- **Hobby Projects**: Custom RC vehicles and automated systems

---

## ✨ Features

### 🎮 **Dual Control Interface**
- **D-Pad Controls**: Traditional button-based navigation (Forward, Backward, Left, Right, Stop)
- **Virtual Joystick**: Smooth analog control with 360-degree movement
- **Toggle Switch**: Seamless switching between control modes

### 🤖 **Advanced Actions**
- **Pick/Drop Controls**: Dedicated buttons for robotic arm operations
- **Multi-Level Lift**: Four lift positions (L1, L2, L3, L4) for precise height control
- **Custom Commands**: Extensible command system for additional features

### 📡 **Robust Bluetooth Connectivity**
- **HC-05 Optimized**: Multiple connection strategies for maximum compatibility
- **Auto-Reconnection**: Intelligent retry mechanism with fallback methods
- **Connection Health Monitoring**: Real-time status monitoring and error recovery
- **Multi-Channel Support**: Automatic channel detection (1-5) for various HC-05 configurations

### 📱 **Modern UI/UX**
- **Material Design 3**: Clean, modern interface following Google's design guidelines
- **Landscape Optimization**: Three-column layout maximizing screen real estate
- **Real-time Feedback**: Live command logging and connection status
- **Responsive Design**: Optimized for tablets and smartphones

### 🔧 **Developer Features**
- **Command Logging**: Real-time display of sent commands with timestamps
- **Connection Diagnostics**: Detailed error messages and troubleshooting info
- **Extensible Architecture**: Easy to add new commands and features

---

## 🛠 Technical Specifications

### **Platform Requirements**
- **Android Version**: 8.0+ (API Level 26+)
- **Architecture**: ARM64, ARM32
- **Bluetooth**: Classic Bluetooth (not BLE)

### **Hardware Compatibility**
- **Primary Target**: HC-05 Bluetooth modules
- **Secondary**: HC-06, ESP32 Bluetooth Classic
- **Communication**: UART/Serial over Bluetooth

### **Technology Stack**
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Service-based Bluetooth management
- **Build System**: Gradle 8.0.2
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

---

## 📥 Installation

### **Option 1: Download APK (Recommended)**
1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from Unknown Sources" in Android Settings
3. Install the APK file
4. Grant Bluetooth and Location permissions when prompted

### **Option 2: Build from Source**

#### **Prerequisites**
- **Android Studio**: Arctic Fox or later
- **JDK**: 8 or later
- **Android SDK**: API level 26+

#### **Build Steps**
```bash
# Clone the repository
git clone https://github.com/Alberick45/bluetoothrccontroller.git
cd bluetoothrccontroller/RoboCarController

# Build using Gradle (Windows)
./gradlew.bat assembleDebug

# Build using Gradle (Linux/Mac)
./gradlew assembleDebug

# Or use the build script
./build_apk.bat
```

**Generated APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

#### **Release Build (Signed)**
For production releases, create a keystore:
```bash
keytool -genkey -v -keystore robocar-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias robocar
```

Then update `app/build.gradle.kts` with your keystore configuration and run:
```bash
./gradlew assembleRelease
```

---

## 🚀 Usage

### **Initial Setup**
1. **Enable Bluetooth**: Ensure Bluetooth is enabled on your Android device
2. **Power HC-05**: Connect and power your HC-05 module
3. **Pair Device**: Pair your phone with the HC-05 (default PIN: 1234 or 0000)

### **Connecting to Your RC Car**
1. Launch the RoboCar Controller app
2. Grant Bluetooth and Location permissions if prompted
3. Select your HC-05 device from the list
4. Wait for "Connected" status confirmation
5. Start controlling your RC car!

### **Control Interface**

#### **Movement Controls (Left Panel)**
- **Forward (F)**: Move car forward
- **Backward (B)**: Move car backward  
- **Left (L)**: Turn car left
- **Right (R)**: Turn car right
- **Stop (S)**: Emergency stop

#### **Action Controls (Center Panel)**
- **Pick (P)**: Activate pick/grab mechanism
- **Drop (D)**: Activate drop/release mechanism
- **L1-L4**: Lift positions (lowest to highest)

#### **Command Log (Right Panel)**
- Real-time display of sent commands
- Timestamp for each command
- Scrollable history (last 50 commands)

### **Command Protocol**
| Command | Function | Description |
|---------|----------|-------------|
| `F` | Forward | Move forward |
| `B` | Backward | Move backward |
| `L` | Left | Turn left |
| `R` | Right | Turn right |
| `S` | Stop | Stop all movement |
| `P` | Pick | Activate picker |
| `D` | Drop | Activate dropper |
| `L1` | Lift Level 1 | Lowest position |
| `L2` | Lift Level 2 | Low position |
| `L3` | Lift Level 3 | High position |
| `L4` | Lift Level 4 | Highest position |

---

## 🔧 Development

### **Project Structure**
```
RoboCarController/
├── app/
│   ├── src/main/java/com/robocar/controller/
│   │   ├── MainActivity.kt              # Main controller UI
│   │   ├── DeviceScanActivity.kt        # Bluetooth device scanning
│   │   ├── bluetooth/
│   │   │   ├── BluetoothManager.kt      # Core Bluetooth logic
│   │   │   └── BluetoothService.kt      # Background service
│   │   └── ui/
│   │       ├── components/
│   │       │   └── CustomJoystick.kt    # Virtual joystick
│   │       └── theme/                   # Material Design theme
│   ├── src/main/res/                    # Resources (layouts, strings, etc.)
│   └── build.gradle.kts                 # App-level build configuration
├── gradle/                              # Gradle wrapper
├── build.gradle.kts                     # Project-level build config
├── build_apk.bat                        # Windows build script
├── build_apk.sh                         # Unix build script  
└── README.md                           # This file
```

### **Key Components**

#### **BluetoothManager.kt**
- Core Bluetooth connectivity logic
- Multiple HC-05 connection strategies
- Command sending with retry mechanism
- Connection health monitoring

#### **MainActivity.kt**
- Main controller interface
- Jetpack Compose UI
- Command logging and user feedback
- Service binding for Bluetooth operations

#### **CustomJoystick.kt**
- Custom Compose joystick implementation
- Touch gesture handling
- Angle and strength calculation
- Smooth movement detection

### **Adding New Features**

#### **Adding New Commands**
1. **Define Command**: Add new command constant
```kotlin
// In MainActivity.kt
fun sendCustomCommand() {
    sendCommand("NEW_COMMAND")
}
```

2. **Add UI Control**: Create button or control element
```kotlin
ControlButton(
    icon = Icons.Default.NewIcon,
    label = "New Action",
    color = Color(0xFF9C27B0),
    onClick = { onCommand("NEW_COMMAND") }
)
```

3. **Handle on Arduino Side**: Process the new command in your Arduino code
```cpp
if (receivedCommand == "NEW_COMMAND") {
    // Implement your new functionality
    performNewAction();
}
```

#### **Modifying Connection Logic**
Edit `BluetoothManager.kt` to add new connection strategies:
```kotlin
// Add new connection method in connectToDevice()
if (!connected) {
    try {
        Log.d(TAG, "Trying custom connection method...")
        // Your custom connection logic here
    } catch (e: Exception) {
        Log.w(TAG, "Custom method failed: ${e.message}")
    }
}
```

### **Building and Testing**

#### **Debug Build**
```bash
./gradlew assembleDebug
```

#### **Release Build**
```bash
./gradlew assembleRelease
```

#### **Running Tests**
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🔍 Troubleshooting

### **Common Issues**

#### **❌ "Failed to connect" Error**
**Possible Causes:**
- HC-05 not in pairing mode
- Device already connected to another phone
- Wrong Bluetooth module type
- Distance too far from device

**Solutions:**
1. Power cycle the HC-05 module
2. Unpair and re-pair the device in phone settings
3. Move closer to the HC-05 module
4. Check HC-05 LED status (should be blinking)
5. Try connecting with a different device to verify HC-05 functionality

#### **❌ "Bluetooth permissions not granted"**
**Solution:**
1. Go to Settings → Apps → RoboCar Controller → Permissions
2. Enable all Bluetooth and Location permissions
3. Restart the app

#### **❌ Commands not working after connection**
**Possible Causes:**
- Baud rate mismatch
- Wrong wiring on Arduino side
- HC-05 configuration issues

**Solutions:**
1. Verify Arduino serial baud rate (typically 9600)
2. Check HC-05 wiring (VCC, GND, RX, TX)
3. Test with Arduino Serial Monitor first
4. Verify HC-05 AT command configuration

#### **❌ Connection drops frequently**
**Solutions:**
1. Check power supply to HC-05 (should be stable 3.3V or 5V)
2. Reduce distance between phone and HC-05
3. Avoid interference from other Bluetooth devices
4. Update to latest app version with improved connection stability

### **Debugging Steps**

1. **Check Logcat**: Use Android Studio to view detailed logs
```bash
adb logcat | grep "BluetoothManager"
```

2. **Test Basic Connection**: Use a Bluetooth terminal app to test HC-05 connectivity

3. **Verify Arduino Code**: Ensure your Arduino is properly receiving and processing commands

4. **Check Hardware**: Verify all connections and power supplies

---

## 🔄 Version History

### **v2.1.0** (Current)
- ✅ Enhanced HC-05 connection stability with multiple fallback strategies
- ✅ Improved landscape layout with optimized three-column design
- ✅ Added connection health monitoring and automatic recovery
- ✅ Implemented command retry mechanism to prevent connection drops
- ✅ Better error messages with specific troubleshooting guidance
- ✅ Reduced UI component sizes for better landscape fit

### **v2.0.0**
- ✅ Complete UI redesign with Jetpack Compose
- ✅ Added virtual joystick control option
- ✅ Implemented Material Design 3 theming
- ✅ Added real-time command logging
- ✅ Improved Bluetooth service architecture

### **v1.0.0**
- ✅ Initial release with basic D-pad controls
- ✅ HC-05 Bluetooth connectivity
- ✅ Pick/Drop and Lift controls
- ✅ Device scanning and pairing

---

## 📦 Dependencies

### **Core Libraries**
- **Jetpack Compose**: Modern Android UI toolkit
- **Material Design 3**: UI components and theming  
- **Kotlin Coroutines**: Asynchronous programming
- **AndroidX Libraries**: Core Android components

### **Custom Components**
- **CustomJoystick**: Native Compose implementation (replaces external virtual joystick library)
- **BluetoothManager**: Enhanced HC-05 connectivity with multiple connection strategies

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### **Reporting Issues**
1. Use the [Issues](../../issues) tab
2. Provide detailed description and steps to reproduce
3. Include device information and Android version
4. Attach relevant logs if possible

### **Submitting Changes**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### **Development Guidelines**
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Test on multiple Android versions
- Update documentation for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Android Team** for Jetpack Compose and Material Design
- **HC-05 Module** manufacturers for reliable Bluetooth hardware
- **Arduino Community** for extensive documentation and support
- **Open Source Contributors** who make projects like this possible

---

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](../../issues)
- **Repository**: https://github.com/Alberick45/bluetoothrccontroller

---

<div align="center">

**⭐ If this project helps you, please consider giving it a star! ⭐**

Made with ❤️ by [Alberick45](https://github.com/Alberick45)

</div>
