# Personal Life Logger

An offline-first Android app for logging personal life entries (title, description, image)
with optional manual sync to Firebase Firestore.

## Stack
- Kotlin + Jetpack Compose (Material 3)
- MVVM with `AndroidViewModel` + `StateFlow`
- Room (offline-first)
- Navigation Compose
- Coil (image loading)
- Firebase Firestore (optional cloud sync)
- Min SDK 24 / Target SDK 34

## Open in Android Studio
1. Unzip the project and choose **File → Open** in Android Studio (Hedgehog or newer).
2. Let Gradle sync. Android Studio will download Gradle 8.7 automatically.
3. Run on an emulator or device (API 24+).

## Firebase setup (required for the cloud-sync button)
The cloud sync button on the home screen will work only after you connect a Firebase project:

1. Go to https://console.firebase.google.com and create a project.
2. Add an Android app with package name **com.example.personallifelogger**.
3. Download the generated `google-services.json` file.
4. Place it inside the `app/` directory of this project.
5. In the Firebase console enable **Firestore Database** (test mode for development).
6. Rebuild and run.

If `google-services.json` is missing the app still runs and stores entries locally
(only the cloud-sync action will fail gracefully).

## Project structure
```
app/
 └─ src/main/
    ├─ AndroidManifest.xml
    ├─ java/com/example/personallifelogger/
    │   ├─ LifeLoggerApp.kt          // Application + simple service locator
    │   ├─ MainActivity.kt           // Compose host
    │   ├─ data/                     // Room (Entry, DAO, DB, Repository)
    │   ├─ viewmodel/EntryViewModel.kt
    │   ├─ navigation/AppNavGraph.kt
    │   ├─ sync/FirebaseSync.kt
    │   └─ ui/
    │       ├─ theme/Theme.kt
    │       └─ screens/ (HomeScreen, AddEntryScreen, EntryDetailScreen)
    └─ res/values/ (strings, themes, colors)
```
