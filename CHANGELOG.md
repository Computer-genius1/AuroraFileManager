# Changelog - Aurora File Manager

All notable changes to the Aurora File Manager project will be documented in this file.

## [v1.0.4] - Aurora File Manager 1.0.4

### 📁 File Management & Usability
- **Scrollable Navigation Drawer**: Added vertical scrolling to side navigation drawer so "Check for Updates" and "About & Release Notes" are always easily accessible on all screen sizes.
- **Browse & Organize**: Copy, cut, paste, move, rename, and delete files/folders with batch selection support.
- **Select All & Selection Counter**: Added a "Select All / Deselect All" toggle and live selection counter.
- **Quick Access Chips**: One-tap navigation chips for Documents, Downloads, Pictures, Photos/DCIM, Videos, Music, and Internal Storage.
- **Filename Validation**: Automatic default naming ("Untitled Folder", "Untitled File.txt") and real-time blank filename validation.
- **One-Tap Search Clear**: Instant search reset button inside the search field.
- **Native Back Navigation**: Jetpack Compose `BackHandler` integration for fluid back gesture handling.

### 🛡️ Root Support & Safety Protection
- **Protected `Android` Folder Guard**: Strict deletion protection preventing accidental deletion of the `/storage/emulated/0/Android` system directory.
- **Samsung Installation & Compatibility**: Fixes installation issues on certain Samsung devices and ROMs.
- **Root Browsing**: `su` shell execution for compatible rooted devices.
- **Mount Point Discovery**: Enhanced storage volume discovery for internal storage, SD cards, and system directories.
- **StatFs Storage Calculation**: Accurately measures physical hardware storage capacity and usable free space.

### 🎨 User Experience & Privacy
- **Automatic Light/Dark System Theme**: Observes system dark mode settings (`isSystemInDarkTheme()`) with Android 12+ dynamic colors.
- **Welcome & Permission Onboarding**: Transparent splash screen explaining required permissions (`MANAGE_EXTERNAL_STORAGE`, `su`, `FileProvider`) and zero-data-collection commitment.
- **Post-Update "What's New" Screen**: Automatic post-update splash screen with feature highlights.
- **Full Spanish Localization**: Complete Spanish (`es`) language translation.
- **Automated GitHub Update Checker**: Queries GitHub API (`Computer-genius1/AuroraFileManager`) to prompt users when new releases drop.
- **FileProvider & MIME Detection**: System "Open with" chooser with automatic MIME type resolution.

### ⚡ Optimization & Security
- **R8 Code & Resource Shrinking**: Optimized release APK compilation.
- **Self-Copy Guards**: Canonical path checks preventing recursive copy loops.
- **Open Source & Private**: No analytics, no data transmission, 100% open source.
