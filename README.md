# WordQuest

WordQuest is an Android vocabulary-learning application built with Kotlin and Jetpack Compose. It retrieves English definitions from the Free Dictionary API, supports flashcard and multiple-choice study modes, and stores learning progress locally.

## Features

- Flashcard and multiple-choice vocabulary sessions
- Configurable daily learning goal
- Dark mode and reminder preferences
- Retry and skip recovery for failed dictionary requests
- Local quiz history with progress, accuracy, and trend summaries
- Dashboard showing today's goal and recent sessions
- Screen-reader-friendly progress and answer feedback

## Technology

- Kotlin and Jetpack Compose
- Material 3 and Navigation Compose
- Hilt dependency injection
- Retrofit and OkHttp networking
- Room local database
- Preferences DataStore
- Coroutines and Flow
- JUnit and MockK tests

Dictionary data is provided by [Free Dictionary API](https://dictionaryapi.dev/). No API key is required.

## Project structure

```text
app/src/main/java/com/example/wordquest/
├── data/
│   ├── api/          Dictionary API models and service
│   ├── local/        Room database, DAO, and entities
│   ├── repository/   Network and persistence coordination
│   └── settings/     DataStore preferences and quiz mode
├── di/               Hilt providers
└── ui/
    ├── model/        Shared progress calculations
    ├── navigation/   Compose destinations
    ├── screens/      Landing, activity, statistics, and settings
    └── theme/        Material color and typography setup
```

The UI observes state from ViewModels. ViewModels coordinate repositories and settings, while the repository keeps API and Room operations outside the UI layer.

## Requirements

- Android Studio with Android SDK 34
- JDK 21, including the runtime bundled with current Android Studio versions
- An emulator or device running Android 8.0 (API 26) or newer
- Internet access for dictionary lookups and the first dependency download

## Run the application

1. Clone the repository:

   ```bash
   git clone https://github.com/S13863709935/CP3406-A3.git
   ```

2. Open the cloned directory in Android Studio.
3. Allow Gradle sync to finish. Android Studio creates the machine-specific `local.properties` file automatically.
4. Start an emulator or connect an Android device.
5. Select the `app` configuration and click **Run**.

If an emulator is listed as `offline`, stop its background emulator process and use **Cold Boot** from Device Manager before running the app again.

## Test and build

On Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

The unit tests cover repository error handling, learning-session state, progress calculations, landing-page summaries, setting persistence, and stored quiz-mode recovery.

## Local data

- Quiz history is stored in the Room database `wordquest_db`.
- Theme, goal, quiz mode, and notification preference are stored with DataStore.
- Clearing history from the statistics screen permanently deletes saved quiz results after confirmation.
- The notification option currently stores the user's preference for a future reminder scheduler.

## Development milestones

1. Added type-safe learning settings and daily-goal boundaries.
2. Hardened dictionary requests and repository error handling.
3. Improved learning-session lifecycle and recovery.
4. Added reusable progress summaries and history controls.
5. Redesigned the home dashboard around learning progress.
6. Improved settings usability, answer feedback, and accessibility.
7. Completed project documentation, boundary tests, and template cleanup.
