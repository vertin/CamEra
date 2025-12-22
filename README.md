# CamEra - Exercise Video Recording App

A small Android application that allows users to record exercise videos, view them with metadata, and manage upload status. Built as part of the Cuju Android Technical Challenge.

## How to Run the Project

### Prerequisites

- Java 17 or higher
- Android Studio (latest stable version recommended)
- Android device or emulator with API level 28 or higher

### Building and Running

1. Clone the repository or extract the project files
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Connect an Android device or start an emulator
5. Run the app using one of the following methods:
   - Click the "Run" button in Android Studio
   - Use the command line: `./gradlew installDebug`
   - Press `Shift+F10` (Windows/Linux) or `Control+R` (macOS)

### Running Tests

- **Unit tests**: `./gradlew test`
- **Instrumented tests**: `./gradlew connectedAndroidTest`

### Building Debug APK

To build a debug APK without installing:
```bash
./gradlew assembleDebug
```

The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`

## Architecture Overview

The application follows the **MVVM (Model-View-ViewModel)** architecture pattern with a clean separation of concerns across multiple layers.

### Layer Structure

- **Presentation Layer**: Jetpack Compose UI screens and ViewModels that manage UI state
- **Domain Layer**: Use cases and domain models representing business logic
- **Data Layer**: Repositories, data sources (Room database), and services for file operations

### Key Components

**ViewModels** handle UI state management and user interactions. They expose state flows that the Compose UI observes reactively.

**Use Cases** encapsulate single business operations. Each use case represents a specific action like saving a video, uploading, or fetching video metadata.

**Repositories** abstract data access and provide a single source of truth. The `ExerciseVideoRepository` manages local database operations, while `UploadVideoRepository` handles upload-related state.

**Room Database** persists video metadata including file paths, timestamps, and upload status. The database uses Paging 3 for efficient list loading.

**WorkManager** handles video uploads as background work. The `VideoUploadWorker` processes uploads with retry logic and status updates.

## Key Decisions

### MVVM Architecture

MVVM was chosen for its clear separation between UI and business logic. ViewModels manage state independently of the UI lifecycle, making the code testable and maintainable. The reactive approach with Kotlin Flows ensures UI updates automatically when data changes.

### Room Database

Room provides type-safe database access with compile-time query verification. It integrates seamlessly with Kotlin coroutines and Flow, enabling reactive data observation. Paging 3 integration allows efficient loading of video lists without loading all items into memory.

### WorkManager for Uploads

WorkManager handles uploads as background work that persists across app restarts. This ensures uploads continue even if the user closes the app. The worker includes retry logic with a maximum attempt limit, and it updates the video status accordingly.

### Media3 (ExoPlayer) for Playback

Media3 provides a modern, feature-rich media playback solution. The Compose integration allows embedding video players directly in the UI with minimal boilerplate. It handles various video formats and provides smooth playback controls.

### CameraX for Recording

CameraX simplifies camera operations with a lifecycle-aware API. It handles device-specific camera implementations automatically and provides a consistent interface across different Android versions. The Compose integration makes it straightforward to embed camera previews in the UI.

### Dependency Injection with Koin

Koin provides lightweight dependency injection without annotation processing overhead. It integrates well with Compose and ViewModels, allowing clean dependency management with minimal setup.

## Libraries and Tools Used

- **Jetpack Compose**: Modern declarative UI framework
- **Room**: Local database persistence
- **Paging 3**: Efficient list loading and pagination
- **WorkManager**: Background work processing
- **Media3 (ExoPlayer)**: Video playback
- **CameraX**: Camera and video recording
- **Koin**: Dependency injection
- **Coil**: Image loading with custom video thumbnail support
- **Kotlin Coroutines**: Asynchronous programming
- **Navigation Compose**: Screen navigation
- **Material 3**: UI components and theming
- **JUnit 5**: Unit testing framework
- **MockK**: Mocking library for tests
- **Turbine**: Flow testing utilities
- **Detekt**: Static code analysis

## Limitations and Assumptions

- Video uploads are simulated locally with a delay. No actual network upload is performed.
- The app requires camera and storage permissions to function properly.
- Videos are stored in the app's internal storage directory.
- The database uses destructive migrations for simplicity. In production, proper migration strategies should be implemented.
- Video thumbnails are generated on-demand and cached by Coil.
- The app targets API level 28 and above (Android 9.0+).
- Maximum upload retry attempts are set to 3 before marking the upload as failed.


