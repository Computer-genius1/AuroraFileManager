# Changelog - Aurora File Manager

All notable changes to the Aurora File Manager project will be documented in this file.

## [v1.5.0] - Windows Beta (2026-11-23)

This release focuses on improvements to the Windows version of Aurora File Manager.

### Windows Improvements
- **Filename validation:** Rejects invalid Windows filename characters, trailing spaces or periods, and reserved device names.
- **Overwrite protection:** Prompts before replacing existing files and reports folder destination conflicts instead of silently merging or overwriting.
- **Safer recursive file operations:** Avoids following symbolic links and junctions during directory copy and deletion.
- **Expanded protected-location safeguards:** Adds protection for Windows directories, Program Files locations, and drive roots.
- **Improved clipboard handling:** Retains items that could not be moved after an unsuccessful cut operation.
- **More reliable navigation:** Recovers more gracefully when a previously visited folder is missing or inaccessible.
- **Responsive folder loading:** Loads directory contents asynchronously and skips individual entries whose metadata cannot be accessed.
- **List performance:** Enables UI virtualization and container recycling for smoother browsing of large folders.
- **File and folder properties:** Adds a properties view with size, path, type, timestamps, and attributes.
- **Keyboard navigation:** Supports pressing Enter to open the selected file or folder.

### Bug Fixes and Reliability
- Improved handling of invalid filenames and conflicting copy destinations.
- Fixed unsafe recursive-operation edge cases involving symbolic links and junctions.
- Improved resilience when individual files or folders cannot be accessed.
- Prevented failed clipboard transfers from unnecessarily discarding pending cut items.
- Reduced UI stalls while loading large directories.
- Improved navigation recovery when folders are removed or become unavailable.

### Compatibility
- Windows 10 and Windows 11.
- x86-64 and ARM64 Windows builds.
- For the best compatibility, we recommend updating to the latest version of Windows available for your PC.

### Other Platforms
**This is a Windows-only update.** Android, iOS, and all other operating-system versions remain unchanged from Beta 2. No platform changes are included for those versions in this release.

### Distribution
- Windows beta installers are currently unsigned; Windows SmartScreen may display an unknown-publisher warning.
- The project remains open source under the Apache License 2.0.


## [v1.0.5] - Aurora File Manager 1.0.5

### 📁 Usability & Bug Fixes
- **Samsung Installation Fix**: Fixed an issue that could block installation on certain Samsung devices and ROMs.
- **Scrollable Navigation Drawer**: Added vertical scrolling to the side navigation drawer so "Check for Updates" and "About & Release Notes" are always easily accessible on all screen sizes.
- **Dynamic Version Inspection**: Automatically inspects installed package version to ensure accurate update detection across devices.
- **Network Error Feedback**: Enhanced update checker to provide clear feedback if network connection fails during update check.

### 📁 File Management & Core Features
- **Browse & Organize**: Copy, cut, paste, move, rename, and delete files/folders with batch selection support.
- **Select All & Selection Counter**: Added a "Select All / Deselect All" toggle and live selection counter.
- **Quick Access Chips**: One-tap navigation chips for Documents, Downloads, Pictures, Photos/DCIM, Videos, Music, and Internal Storage.
- **Filename Validation**: Automatic default naming ("Untitled Folder", "Untitled File.txt") and real-time blank filename validation.
- **One-Tap Search Clear**: Instant search reset button inside the search field.
- **Native Back Navigation**: Jetpack Compose `BackHandler` integration for fluid back gesture handling.

### 🛡️ Root Support & Safety Protection
- **Protected `Android` Folder Guard**: Strict deletion protection preventing accidental deletion of the `/storage/emulated/0/Android` system directory.
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
