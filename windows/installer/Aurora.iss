#ifndef AppArch
  #define AppArch "x64"
#endif
#define AppVersion "0.1.0"
#define AppName "Aurora File Manager"
[Setup]
AppId={{9B3B1B0A-3D5A-4B12-9A9A-3E2A2A5A0B71}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher=Aurora File Manager
DefaultDirName={autopf}\Aurora File Manager
DefaultGroupName=Aurora File Manager
OutputDir=..\dist\installers
OutputBaseFilename=Aurora-File-Manager-Windows-{#AppArch}-Beta-0.1.0-Setup
#if AppArch == "x64"
ArchitecturesAllowed=x64compatible and not arm64
#else
ArchitecturesAllowed=arm64
#endif
PrivilegesRequired=lowest
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
UninstallDisplayIcon={app}\AuroraFileManager.exe
[Files]
Source: "..\dist\{#AppArch}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
[Icons]
Name: "{autoprograms}\Aurora File Manager"; Filename: "{app}\AuroraFileManager.exe"
Name: "{autodesktop}\Aurora File Manager"; Filename: "{app}\AuroraFileManager.exe"; Tasks: desktopicon
[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"
[Run]
Filename: "{app}\AuroraFileManager.exe"; Description: "Launch Aurora File Manager"; Flags: postinstall nowait skipifsilent
