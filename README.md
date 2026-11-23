# Aurora File Manager 📁

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Latest Release](https://img.shields.io/badge/Release-v1.0.5-brightgreen.svg)](https://github.com/Computer-genius1/AuroraFileManager/releases)
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_iOS_(Beta)_%7C_Win_11_(Beta)-orange.svg)](https://github.com/Computer-genius1/AuroraFileManager)
[![Languages](https://img.shields.io/badge/Languages-Kotlin_%7C_Swift_%7C_C%23_--_C%2B%2B-purple.svg)](https://github.com/Computer-genius1/AuroraFileManager)
[![Architectures](https://img.shields.io/badge/Architectures-ARM64_%7C_x86__64-blue.svg)](https://github.com/Computer-genius1/AuroraFileManager)

**Aurora File Manager** is a modern, transparent, and 100% open-source cross-platform file manager for **Android**, **iOS (Beta)**, and **Windows 11 (Beta - x86_64 & ARM64)**. Built with native, high-performance UI frameworks (**Jetpack Compose**, **SwiftUI**, and **WinUI 3**), Aurora provides full filesystem control without tracking, ads, or cloud data collection.

> [!NOTE]  
> **Beta Platforms Notice**: Support for **iOS**, **Windows 11 (x86_64)**, and **Windows 11 (ARM64)** is currently in **Early Access Beta**.

---

## ✨ Features

### 📁 File Management & Usability
- **Cross-Platform Support**: Native experience optimized for **Android** (Stable), **iOS** (Beta), **Windows 11 x86_64** (Beta), and **Windows 11 ARM64** (Beta).
- **Complete File Operations**: Browse, copy, cut, paste, move, rename, and delete files and folders.
- **Batch Clipboard & Multi-Selection**: Select multiple items with live item counter and one-tap **Select All / Deselect All** controls.
- **Quick Access Filter Chips Row**: One-tap access to Documents, Downloads, Pictures, Photos/DCIM, Videos, Music, and Storage directly below path breadcrumbs.
- **Default Filename & Blank Validation**: Automatically pre-fills "Untitled Folder" and "Untitled File.txt" with real-time error validation if blank.
- **Settings & Custom Themes**: Easily switch between **System Default**, **Light Theme**, or **Dark Theme** in the side settings menu.
- **Native Back Navigation**: Native gesture and key handling across drawers, search, selection, and directory levels.

### 🛡️ System Engine & Platform Protection
- **Protected Directory Guard**: Strict deletion protection preventing accidental deletion of vital system folders.
- **Root & Extended Browsing**: Superuser shell (`su`) support for compatible Android devices, extended file access on iOS, and system privilege management on Windows 11.
- **Physical Storage Calculation**: Accurately measures physical hardware storage capacity and usable free space across internal and external volumes.

### 🎨 User Experience & Privacy
- **Automatic System Dark Theme**: Built-in theme engine observing system night settings with Android 12+ dynamic colors, iOS native dark mode, and Windows 11 Mica / Dark theme.
- **Welcome & Permission Onboarding**: Transparent first-launch onboarding explaining required permissions and zero-data-collection commitment.
- **Multi-Language Support**: Full localization support for English, Spanish, French, and Standard Simplified Chinese.
- **File Sharing & MIME Resolution**: System file sharing with automatic MIME type resolution.

---

## 🔒 Privacy Commitment

Aurora File Manager is committed to 100% privacy and transparency:
- ❌ **Zero Data Collection**: No user tracking or telemetries.
- ❌ **No Cloud Uploads**: Your files never leave your device.
- ❌ **No Ads or In-App Purchases**: 100% free and open-source.
- ✅ **100% Open Source**: Fully inspectable codebases across all supported platforms.

---

## 🛠️ Building from Source

### Prerequisites
- **Android**: Android Studio Ladybug or newer, JDK 17/21, Android SDK 35/37
- **iOS (Beta)**: macOS, Xcode 15+, iOS 16+ SDK
- **Windows 11 (Beta - x86_64 / ARM64)**: Windows 11, Visual Studio 2022+ with C# / C++ WinUI 3 workloads

### Build Commands

#### Android (Kotlin & Jetpack Compose - Stable):
```bash
git clone https://github.com/Computer-genius1/AuroraFileManager.git
cd AuroraFileManager

# Run Android unit tests
./gradlew testDebugUnitTest

# Assemble Android Release APK
./gradlew assembleRelease
```
The compiled Android APK is output to `app/build/outputs/apk/release/app-release.apk`.

#### iOS (Swift & SwiftUI - Beta):
Open the iOS project folder in **Xcode** and build for simulator or device:
```bash
xcodebuild -scheme AuroraFileManager -configuration Release
```

#### Windows 11 (x86_64 & ARM64 - Beta):
Open the Windows solution file in **Visual Studio 2022** or build via MSBuild:
```powershell
msbuild AuroraFileManager.sln /p:Configuration=Release /p:Platform="x64"   # x86_64
msbuild AuroraFileManager.sln /p:Configuration=Release /p:Platform="ARM64" # ARM64
```

---

## 📄 Open Source License

Distributed under the **Apache License 2.0**. See [`LICENSE`](LICENSE) for details.
