# Aurora File Manager for Windows 1.5 Beta

A native Windows desktop companion to Aurora File Manager. This 1.5 beta focuses on more reliable everyday file browsing and safer file operations.

## Supported systems and installers

- Windows 10 (version 1809 or later) and Windows 11, x64 (Intel/AMD 64-bit)
- Windows 10 (version 1809 or later) and Windows 11, ARM64 (Windows on ARM)

Each installer is a self-contained .NET 8 WPF application and does not require users to install the .NET runtime separately.

## First beta features

- Quick access to Home, Desktop, Documents, Downloads, Pictures, Music, Videos, and This PC
- Browse folders and open files with their default Windows application
- Search within the current folder, with results correctly updating as the query changes
- Create folders and files
- Copy, cut, paste, rename, and delete selected items
- Show or hide hidden files
- Keyboard shortcuts: Ctrl+C, Ctrl+X, Ctrl+V, Delete, F5, and Backspace
- Guardrails that block deletion of Windows, Program Files, and drive-root locations; recursive folder copy skips reparse points to avoid following directory links

## Platform and safety notes

Aurora uses the current Windows user's normal file permissions. It does not silently elevate privileges, bypass access controls, or provide Android-style root access. Some protected folders will display Access Denied unless Windows permissions allow access. Deletion is permanent in this beta and asks for confirmation first.

This is an early beta. Test with non-important files and keep backups. The Windows app does not upload files or collect telemetry.

## Build

Requires Windows, .NET 8 SDK, and Inno Setup 6.3 or newer.

```powershell
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -o dist/x64
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-arm64 --self-contained true -p:PublishSingleFile=true -o dist/arm64
```

GitHub Actions builds two independent installers:
- `Aurora-File-Manager-Windows-x64-Beta-0.1.0-Setup.exe`
- `Aurora-File-Manager-Windows-arm64-Beta-0.1.0-Setup.exe`

The installers are unsigned in this beta; Windows SmartScreen may show an unknown-publisher warning until a code-signing certificate is used.
