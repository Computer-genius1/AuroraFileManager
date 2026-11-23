# Aurora File Manager 📁

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Latest Release](https://img.shields.io/badge/Release-v1.0.5-brightgreen.svg)](https://github.com/Computer-genius1/AuroraFileManager/releases)
[![Platforms](https://img.shields.io/badge/Platforms-Android_%7C_iOS_Beta_%7C_Windows_10%2F11_Beta-orange.svg)](https://github.com/Computer-genius1/AuroraFileManager)
[![Languages](https://img.shields.io/badge/Languages-Kotlin_%7C_Swift_%7C_C%23_%7C_XAML_%7C_XML_%7C_YAML_%7C_Inno_Setup-purple.svg)](https://github.com/Computer-genius1/AuroraFileManager)
[![Architectures](https://img.shields.io/badge/Architectures-ARM64_%7C_x86__64-blue.svg)](https://github.com/Computer-genius1/AuroraFileManager)

**Aurora File Manager** is a free, open-source cross-platform file manager for **Android** (stable), **iOS** (beta), and **Windows 10/11** (beta, x86-64 and ARM64). Each platform uses its native UI technology: **Kotlin and Jetpack Compose** on Android, **Swift and SwiftUI** on iOS, and **C# with WPF/XAML** on Windows. Aurora is designed to keep file operations on your device, without ads, tracking, or cloud file uploads.

> [!NOTE]  
> **Beta Platforms Notice**: The iOS and Windows 10/11 editions are early-access betas. Windows builds are provided separately for x86-64 (Intel/AMD) and ARM64 devices.

---

## ✨ Features

### 📁 File Management & Usability
- **Cross-Platform Support**: **Android** (Stable), **iOS 15 and later** (Beta), and **Windows 10/11** on **x86-64** and **ARM64** (Beta).
- **Complete File Operations**: Browse, copy, cut, paste, move, rename, and delete files and folders.
- **Batch Clipboard & Multi-Selection**: Select multiple items with live item counter and one-tap **Select All / Deselect All** controls.
- **Quick Access Filter Chips Row**: One-tap access to Documents, Downloads, Pictures, Photos/DCIM, Videos, Music, and Storage directly below path breadcrumbs.
- **Default Filename & Blank Validation**: Automatically pre-fills "Untitled Folder" and "Untitled File.txt" with real-time error validation if blank.
- **Settings & Custom Themes**: Easily switch between **System Default**, **Light Theme**, or **Dark Theme** in the side settings menu.
- **Native Back Navigation**: Native gesture and key handling across drawers, search, selection, and directory levels.

### 🛡️ System Engine & Platform Protection
- **Protected Directory Guard**: Strict deletion protection preventing accidental deletion of vital system folders.
- **Platform-Appropriate File Access**: Optional superuser (`su`) access on compatible rooted Android devices. iOS uses Apple's app sandbox and document picker; Windows uses the signed-in user's normal Windows permissions.
- **Physical Storage Calculation**: Accurately measures physical hardware storage capacity and usable free space across internal and external volumes.

### 🎨 User Experience & Privacy
- **Native Appearance**: Android system theme support and native iOS appearance. Windows uses the standard Windows desktop environment.
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
- **iOS (Beta)**: macOS, Xcode 15+, and XcodeGen; deployment target iOS 15.0.
- **Windows (Beta - x86-64 / ARM64)**: Windows 10 version 1809 or later, or Windows 11; .NET 8 SDK and Inno Setup 6.3+ for installer builds.

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
Generate the Xcode project and build the app (requires macOS and Xcode):
```bash
brew install xcodegen
cd ios
xcodegen generate
xcodebuild -project AuroraFileManager.xcodeproj -scheme AuroraFileManager -configuration Release -sdk iphoneos CODE_SIGNING_ALLOWED=NO build
```

#### Installing the iOS Beta (without building from source)

You don't need to open Xcode to try Aurora on a compatible iPhone or iPad. You can install the iOS beta using a sideloading tool:

1. Download the **unsigned iOS beta IPA** from the iOS GitHub Actions build artifact or from an iOS beta GitHub Release when one is published.
2. Choose a sideloading tool:
   - **[Sideloadly](https://sideloadly.io/)** — install the IPA from a Mac or Windows PC.
   - **[AltStore](https://altstore.io/)** — install and manage the IPA using AltStore.
   - **[SideStore](https://sidestore.io/)** — an alternative iOS sideloading option.
3. Follow the selected tool's instructions to sign the IPA with your Apple account and install it on your device.
4. If iOS asks you to trust the developer profile, follow Apple's on-device trust instructions before opening the app.

**Important:** The beta IPA is distributed unsigned. It must be signed during installation by your chosen sideloading tool; simply downloading or tapping the IPA will not install it. Sideloading may require a computer for initial setup, and free Apple account signing can have provisioning and refresh limitations. Only install builds you trust.

#### Windows 10/11 (C#, WPF & XAML - Beta):
Build self-contained applications for each architecture with the .NET 8 SDK:
```powershell
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -o dist/x64
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-arm64 --self-contained true -p:PublishSingleFile=true -o dist/arm64
```
Use Inno Setup 6.3+ and `windows/installer/Aurora.iss` to package the Windows installers. GitHub Actions also builds the x64 and ARM64 installers automatically.

---

## 📄 Open Source License

Distributed under the **Apache License 2.0**. See [`LICENSE`](LICENSE) for details.
