@echo off
echo Building RoboCar Controller APK...
echo.

echo Cleaning previous builds...
call gradlew.bat clean

echo.
echo Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Debug APK built successfully!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    choice /C YN /M "Build release APK as well? (Y/N)"
    if !ERRORLEVEL! EQU 1 (
        echo Building release APK...
        call gradlew.bat assembleRelease
        if %ERRORLEVEL% EQU 0 (
            echo.
            echo ✓ Release APK built successfully!
            echo Location: app\build\outputs\apk\release\app-release.apk
        ) else (
            echo ✗ Release build failed!
        )
    )
) else (
    echo ✗ Debug build failed!
)

echo.
echo Build complete!
pause