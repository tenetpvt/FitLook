# FitLook

Welcome to **FitLook**, a fashion discovery Android application! FitLook allows users to browse outfit curations, get AI-powered visual summaries of styles, and easily discover directly shoppable links to recreate their favorite looks.

## Features

*   ✨ **AI Outfit Analysis**: Powered by Google's Gemini AI, FitLook can analyze an outfit image and automatically generate a catchy title, description, and style category.
*   🛒 **Shoppable Links**: Every outfit card contains direct links to purchase the individual clothing items.
*   🔥 **Trending & Style Discovery**: Browse through feeds of "Featured This Week", "Trending Looks", and filter specifically by styles like Vintage, Streetwear, and Formal. 
*   💾 **Save Favorites**: Double tap your favorite look to save it into your personal collection.
*   🎨 **Premium UI**: Built entirely in Jetpack Compose, the app boasts a modern, dynamic dark theme with smooth scroll experiences and stunning carousels.

## Technology Stack

*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/)
*   **Database backend**: [Firebase Firestore](https://firebase.google.com/docs/firestore)
*   **AI Engine**: [Google Gemini SDK](https://ai.google.dev/docs)

## Getting Started

To build and run the app locally, you need to configure a couple of sensitive files which are strictly excluded from version control.

### 1. Firebase Configuration

FitLook uses Firebase Firestore to store outfit metadata.

1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Create a new project (or use an existing one).
3.  Add an Android app to the project with the package name: `com.example.fitlook`.
4.  Download the generated `google-services.json` file.
5.  Place the `google-services.json` file in the `app/` directory of this project (`FitLook/app/google-services.json`).
6.  Enable Cloud Firestore in your Firebase project and set the rules to allow read/write access (for testing, you can use test mode).

### 2. Gemini API Key

FitLook uses the Gemini API for the "Add Outfit" AI generation screen.

1.  Go to [Google AI Studio](https://aistudio.google.com/).
2.  Get an API key.
3.  Open the `local.properties` file located at the root layer of your project (if not present, create it).
4.  Add the following line to the file:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```

### 3. Build & Run

*   Open the project in **Android Studio**.
*   Wait for a Gradle Sync.
*   Run the app on an Android device or emulator (API 24+).

---

Built for innovation. Happy Styling! 🚀
