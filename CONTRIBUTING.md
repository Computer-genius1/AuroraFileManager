# Contributing to Aurora File Manager

Thank you for your interest in contributing to Aurora File Manager for **Android** and **iOS**! We welcome pull requests, bug reports, feature suggestions, and localization contributions.

## How to Contribute

### 1. Reporting Bugs
- Open an Issue on GitHub: [Issues Page](https://github.com/Computer-genius1/AuroraFileManager/issues)
- Provide device details (Android/iOS version, device model) and steps to reproduce the issue.

### 2. Feature Requests
- Open an Issue tagged as `enhancement`.
- Describe the feature and why it would benefit users across Android and iOS.

### 3. Submitting Pull Requests
1. Fork the repository on GitHub.
2. Create a feature branch (`git checkout -b feature/my-feature`).
3. Ensure code formatting aligns with standard Kotlin (Android) or Swift (iOS) conventions and builds cleanly:
   - **Android**: `./gradlew testDebugUnitTest && ./gradlew assembleDebug`
   - **iOS**: `xcodebuild -scheme AuroraFileManager -configuration Debug`
4. Commit your changes and open a Pull Request against the `main` branch.
