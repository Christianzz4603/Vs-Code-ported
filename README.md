# Code Studio - VS Code Port for Android

An advanced Visual Studio Code port for Android built with Kotlin, Jetpack Compose, Room Database, and Gemini AI Workspace Copilot.

## Key Features

- **Full VS Code UI Layout**: Activity Bar, File Explorer, Command Palette (`Ctrl+Shift+P`), Split Editor, Interactive Terminal, Extensions Gallery, and Status Bar.
- **Full Workspace Gemini AI Copilot**: Real-time indexing of all workspace files, symbols, imports, and diagnostics with support for multi-file edits, preview diffs, accept/reject, and full rollback.
- **Git Source Control & Sync Commit**: Stage modified files, write commit messages, and execute one-click Sync Commits (`git add`, `git commit`, `git push`) from the Source Control sidebar or Command Palette.
- **Local Data Persistence**: Powered by Room DB with fast offline state saving and file management.

## Build Instructions

### Local Build (Gradle)

```bash
# Make gradlew executable
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The compiled APK will be located at `app/build/outputs/apk/release/app-release.apk`.

### GitHub Actions CI/CD

This repository includes automated workflows under `.github/workflows/`:
- `ci.yml`: Runs automated compilation and unit testing on pull requests and pushes.
- `android-build-release.yml`: Builds release APK artifacts automatically on main branch updates.
