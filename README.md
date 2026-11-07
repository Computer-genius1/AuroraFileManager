# Aurora File Manager 📁

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Latest Release](https://img.shields.io/badge/Release-v1.0.4-brightgreen.svg)](https://github.com/Computer-genius1/AuroraFileManager/releases)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-orange.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin_100%25-purple.svg)](https://kotlinlang.org)

**Aurora File Manager** is a modern, transparent, and 100% open-source Android file manager built with **Jetpack Compose** and **Material 3**. Designed around privacy, usability, and speed, Aurora provides full filesystem control without tracking, ads, or data collection.

---

## ✨ Features

### 📁 File Management & Usability
- **Complete File Operations**: Browse, copy, cut, paste, move, rename, and delete files and folders.
- **Batch Clipboard & Multi-Selection**: Select multiple items with live item counter and one-tap **Select All / Deselect All** controls.
- **Quick Access Filter Chips Row**: One-tap access to Documents, Downloads, Pictures, Photos/DCIM, Videos, Music, and Internal Storage directly below the path breadcrumbs.
- **Default Filename & Blank Validation**: Automatically pre-fills "Untitled Folder" and "Untitled File.txt" with real-time error validation if blank.
- **One-Tap Search Clear**: Clear search text with a single tap.
- **Native Back Navigation**: Jetpack Compose `BackHandler` integration for fluid back gesture handling across drawers, search, selection, and directory levels.

### 🛡️ Root Support & System Engine
- **Protected `Android` Directory Guard**: Strict deletion protection preventing accidental deletion of the `/storage/emulated/0/Android` system directory.
- **Root Superuser Shell (`su`)**: Seamless browsing and management of protected system partitions (`/data`, `/system`, SD cards) on compatible rooted devices.
- **StatFs Physical Storage Calculation**: Accurately measures physical hardware storage capacity and usable free space across internal and external volumes.

### 🎨 User Experience & Privacy
- **Automatic System Dark Theme**: Built-in `AuroraTheme` observing system night settings (`isSystemInDarkTheme()`) with Android 12+ dynamic colors.
- **Welcome & Permission Onboarding**: Transparent first-launch onboarding screen explaining required permissions (`MANAGE_EXTERNAL_STORAGE`, `su`, `FileProvider`) and zero-data-collection commitment.
- **Full Spanish Language Support**: Complete Spanish (`es`) localization for all interface elements and dialogs.
- **Automated GitHub Release Update Checker**: Background update checker querying the GitHub API (`Computer-genius1/AuroraFileManager`) to notify users when new releases drop.
- **FileProvider & MIME Resolution**: System "Open with" chooser supporting text, code, PDF, image, audio, and video files.

---

## 🔒 Privacy Commitment

Aurora File Manager is committed to 100% privacy and transparency:
- ❌ **Zero Data Collection**: No user tracking or telemetries.
- ❌ **No Cloud Uploads**: Your files never leave your device.
- ❌ **No Ads or In-App Purchases**: 100% free and open-source.
- ✅ **100% Open Source**: Fully inspectable Kotlin codebase.

---

## 🛠️ Building from Source

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17 / 21
- Android SDK 35/37

### Build Commands
Clone the repository and build the Release APK:

```bash
git clone https://github.com/Computer-genius1/AuroraFileManager.git
cd AuroraFileManager

# Run host unit tests
./gradlew testDebugUnitTest

# Assemble Release APK
./gradlew assembleRelease
```

The compiled APK will be output to `app/build/outputs/apk/release/app-release.apk`.

---

## 📄 Open Source License

Distributed under the **Apache License 2.0**. See [`LICENSE`](LICENSE) for details.
