# Aurora File Manager for iOS

Native SwiftUI companion app for the Android Aurora File Manager project.

## Compatibility
- Deployment target: iOS 15.0
- Intended to support iOS 15.8.x devices, including iPhone 6s, subject to testing on physical hardware.
- Built with SwiftUI and system document interfaces.

## Current implementation
- Browse Aurora's private app Documents directory.
- Import files from the Files app using Apple's document picker.
- Create folders and text files.
- Copy, cut, paste, rename, and delete files and folders.
- Multi-select and select all.
- Search and file-type quick filters.
- Share/open files through the iOS share sheet.
- System light/dark appearance.
- File sharing and opening in place enabled for the app's Documents directory.

## iOS security and platform differences
iOS apps are sandboxed. This app cannot obtain root access, browse protected iOS system directories, or silently enumerate all files on the device. Users explicitly select external files or folders through Apple's document interfaces. Access to selected external locations is governed by iOS security-scoped URLs.

The app does not upload files or send file contents to a server. The GitHub release workflow only builds and publishes the app binary. Do not treat this initial port as a security-audited product; review and test it before storing highly sensitive material.

Files created in the app's Documents directory use iOS file-protection APIs. iOS device passcode, encryption, and the device's lock state remain important parts of the security model.

## Build locally
Requires macOS and Xcode:
```sh
brew install xcodegen
cd ios
xcodegen generate
xcodebuild -project AuroraFileManager.xcodeproj -scheme AuroraFileManager -configuration Release -sdk iphoneos CODE_SIGNING_ALLOWED=NO build
```

## GitHub Actions IPA
The workflow at `.github/workflows/ios-ipa.yml` builds an unsigned IPA on manual dispatch. Pushing a tag such as `ios-v0.1.0` also creates a GitHub Release and attaches the IPA.

**The generated IPA is unsigned.** Installing on a physical iPhone requires signing with an Apple Development/Distribution certificate and a matching provisioning profile, or signing through a supported sideloading workflow. The GitHub Actions workflow does not bypass Apple's signing requirements.
