# Aurora File Manager for Windows 1.5 Beta

A native Windows desktop companion to Aurora File Manager. This 1.5 beta focuses on more reliable everyday file browsing, safer file operations, and better handling of large folders.

## Supported systems and installers

- Windows 10 (version 1809 or later) and Windows 11, x64 (Intel/AMD 64-bit)
- Windows 10 (version 1809 or later) and Windows 11, ARM64 (Windows on ARM)

Each installer is a self-contained .NET 8 WPF application and does not require users to install the .NET runtime separately.

## Features

- Quick access to Home, Desktop, Documents, Downloads, Pictures, Music, Videos, and This PC
- Browse folders and open files with their default Windows application
- Asynchronous folder loading and virtualized list rendering to keep the interface responsive in large folders
- Search within the current folder, with results correctly updating as the query changes
- Create folders and files with Windows filename validation
- Copy, cut, paste, rename, and permanently delete selected items
- Confirmation before replacing existing files; existing folders are never silently merged or overwritten
- Properties view for file/folder location, size (files), dates, and attributes
- Show or hide hidden files
- Keyboard shortcuts: Ctrl+C, Ctrl+X, Ctrl+V, Delete, F5, Backspace, and Enter
- Guardrails that block deletion of Windows, Program Files, and drive-root locations
- Recursive copy and deletion handle reparse points without traversing directory links

## Platform and safety notes

Aurora uses the current Windows user's normal file permissions. It does not silently elevate privileges, bypass access controls, or provide Android-style root access. Some protected folders will display Access Denied unless Windows permissions allow access. Deletion is permanent and asks for confirmation first. Always verify the selected path before confirming deletion and keep backups of important data.

The Windows app does not upload files or collect telemetry.

## Build

Requires Windows, .NET 8 SDK, and Inno Setup 6.3 or newer.

```powershell
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -o dist/x64
dotnet publish windows/AuroraFileManager/AuroraFileManager.csproj -c Release -r win-arm64 --self-contained true -p:PublishSingleFile=true -o dist/arm64
```

GitHub Actions builds two independent installers:
- `Aurora-File-Manager-Windows-x64-Beta-1.5.0-Setup.exe`
- `Aurora-File-Manager-Windows-arm64-Beta-1.5.0-Setup.exe`

The installers are unsigned in this beta; Windows SmartScreen may show an unknown-publisher warning until a code-signing certificate is used.
