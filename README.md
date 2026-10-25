# Secure Digital Vault

A native Android application designed to keep important personal information organized in one place, with access controls and privacy-focused features.

## Overview

Secure Digital Vault provides a centralized place to manage sensitive personal records, including passwords, notes, identity documents, cards, recovery codes, private photos, and document files.

The application includes PIN-based access, optional biometric authentication, configurable automatic locking, and screenshot protection for supported Android capture paths.

## Features

- **PIN authentication:** Set up and use a PIN to access the vault.
- **Biometric authentication:** Enable biometric access on supported devices.
- **Automatic locking:** Configure the timeout before the app locks after being in the background.
- **Password management:** Add, view, and manage saved password entries.
- **Private notes:** Keep personal notes organized.
- **Identity documents:** Manage saved identity-document information.
- **Card management:** Organize card-related information.
- **Recovery codes:** Store and manage recovery codes.
- **Private photos:** Import and view photos within the application.
- **Document management:** Import, search, open, and manage saved document files.
- **Global search:** Search across supported vault content.
- **Settings:** Manage supported security and application preferences.
- **Screenshot protection:** Uses Android's `FLAG_SECURE` to block standard screenshots and restrict ordinary screen capture of protected app windows.

## Tech Stack

- **Language:** Java
- **Platform:** Android
- **UI:** Android XML layouts
- **IDE:** Android Studio
- **Security APIs:** Android window security flags and biometric authentication APIs

## Getting Started

### Prerequisites

- Android Studio
- Android SDK compatible with the project's Gradle configuration
- A compatible Android emulator or physical Android device
- JDK version supported by the project's Gradle configuration

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/YOUR-USERNAME/secure-digital-vault.git
   ```

2. Open Android Studio.
3. Select **Open** and choose the cloned project folder.
4. Allow Gradle to sync and download the required dependencies.
5. Select an Android emulator or connect a physical Android device.
6. Click **Run** to build, install, and launch the application.

Replace `YOUR-USERNAME` with your GitHub username.

## Security Notes

- Screenshot protection relies on Android's `FLAG_SECURE`. It does not guarantee protection against every privileged, external, or host-level capture method.
- Biometric availability depends on the device and its enrolled authentication methods.
- Keep signing keys, passwords, API tokens, local configuration secrets, and private test data out of version control.
- Review the application's storage and backup configuration before storing real sensitive information.
- This project's security features should not be treated as a substitute for an independent security audit.

## Project Structure

The application organizes its code into areas such as:

- `data/` — data access and persistence
- `data/prefs/` — application preferences
- `ui/` — screens for authentication, onboarding, dashboard, passwords, notes, documents, cards, photos, search, and settings
- `util/` — shared utility classes

The exact files and package structure may evolve as the project develops.

## Future Improvements

Potential improvements include expanded automated security tests, additional backup and recovery safeguards, accessibility enhancements, and ongoing security reviews.

## Author

**Abhilasha**

## License

No license has been specified yet. All rights are reserved by default unless a license is added to this repository.
