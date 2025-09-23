# RoboCar Controller

A Bluetooth-enabled Android application for controlling a robot car via HC-05 Bluetooth module.

## Features

- **Bluetooth Connectivity**: Connects to HC-05 Bluetooth modules
- **Dual Control Modes**: 
  - D-pad style buttons for precise control
  - Virtual joystick for analog movement
- **Robot Commands**:
  - Movement: Forward (F), Backward (B), Left (L), Right (R), Stop (S)
  - Actions: Pick (P), Drop (D)
  - Lift Controls: L1, L2, L3, L4
- **Command Logging**: Real-time display of sent commands
- **Auto-reconnect**: Saves last connected device for quick reconnection
- **Error Handling**: Connection retry and status monitoring

## Requirements

- Android 8.0+ (API level 26)
- Bluetooth support
- Location permissions for Bluetooth device discovery

## Building the Project

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8 or later
- Android SDK with API level 26+

### Build Steps

1. **Clone/Download the project**
   ```
   git clone &lt;your-repo-url&gt;
   cd RoboCarController
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open the project folder
   - Wait for Gradle sync to complete

3. **Build Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release APK**
   
   For unsigned release:
   ```bash
   ./gradlew assembleRelease
   ```
   
   For signed release (requires keystore setup):
   - Uncomment the `signingConfigs` section in `app/build.gradle.kts`
   - Add your keystore details
   - Run: `./gradlew assembleRelease`

### Keystore Generation (for signed APK)

```bash
keytool -genkey -v -keystore robocar-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias robocar
```

Then update `app/build.gradle.kts` with your keystore path and credentials.

## Installation

1. Enable "Unknown Sources" or "Install from Unknown Sources" in Android settings
2. Install the APK: `adb install app-debug.apk` or transfer and install manually
3. Grant required permissions when prompted

## Usage

### First Time Setup
1. Launch the app
2. Grant Bluetooth and Location permissions
3. Enable Bluetooth if prompted
4. Select your HC-05 device from the list
5. Connect (default PIN: 1234)

### Robot Control
- **Button Mode**: Use D-pad buttons for movement and action buttons
- **Joystick Mode**: Toggle to joystick view for analog control
- **Commands**: All commands are logged in real-time
- **Disconnect**: Use the disconnect button to return to device selection

### Robot Commands Sent
- `F` - Move Forward
- `B` - Move Backward  
- `L` - Turn Left
- `R` - Turn Right
- `S` - Stop
- `P` - Pick/Grab
- `D` - Drop/Release
- `L1`, `L2`, `L3`, `L4` - Lift positions

## Troubleshooting

### Connection Issues
- Ensure HC-05 is in pairing mode
- Check if device is already paired in Android Bluetooth settings
- Verify HC-05 PIN (usually 1234 or 0000)
- Try clearing Bluetooth cache in Android settings

### Permission Issues
- Grant all requested permissions in app settings
- Enable Location services for Bluetooth scanning
- Ensure app has Bluetooth access

### Build Issues
- Clean project: `./gradlew clean`
- Check Android SDK and build tools versions
- Verify internet connection for dependency downloads

## Project Structure

```
app/
├── src/main/
│   ├── java/com/robocar/controller/
│   │   ├── MainActivity.kt              # Main control interface
│   │   ├── DeviceScanActivity.kt        # Bluetooth device scanning
│   │   ├── bluetooth/
│   │   │   ├── BluetoothManager.kt      # Bluetooth operations
│   │   │   └── BluetoothService.kt      # Background service
│   │   └── ui/theme/                    # UI theming
│   ├── res/                             # App resources
│   └── AndroidManifest.xml              # App permissions and components
├── build.gradle.kts                     # App build configuration
└── proguard-rules.pro                   # ProGuard rules for release builds
```

## Dependencies

- **Jetpack Compose**: Modern Android UI toolkit
- **Material Design 3**: UI components and theming
- **Virtual Joystick**: `io.github.controlwear:virtualjoystick:1.10.1`
- **Kotlin Coroutines**: Asynchronous programming
- **AndroidX Libraries**: Core Android components

## License

This project is open source. Please ensure you comply with all dependency licenses when distributing.

## Contributing

1. Fork the project
2. Create a feature branch
3. Make your changes
4. Test on physical Android device with HC-05
5. Submit a pull request

## Hardware Compatibility

**Tested Bluetooth Modules:**
- HC-05 Classic Bluetooth
- HC-06 (similar to HC-05)

**Android Versions:**
- Android 8.0 (API 26) and higher
- Tested on Android 9, 10, 11, 12, 13

**Robot Integration:**
The app sends simple single-character commands perfect for MicroPython or Arduino-based robot controllers.