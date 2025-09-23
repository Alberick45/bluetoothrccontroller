#!/bin/bash
echo "Building RoboCar Controller APK..."
echo

echo "Cleaning previous builds..."
./gradlew clean

echo
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo
    echo "✓ Debug APK built successfully!"
    echo "Location: app/build/outputs/apk/debug/app-debug.apk"
    echo
    
    read -p "Build release APK as well? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Building release APK..."
        ./gradlew assembleRelease
        if [ $? -eq 0 ]; then
            echo
            echo "✓ Release APK built successfully!"
            echo "Location: app/build/outputs/apk/release/app-release.apk"
        else
            echo "✗ Release build failed!"
        fi
    fi
else
    echo "✗ Debug build failed!"
fi

echo
echo "Build complete!"