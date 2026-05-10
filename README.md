# DefineEasy
![Define Easy](https://github.com/rudradave1/DefineEasy/assets/35660907/ffc8e210-8e07-44c0-bb65-c33495f86aee)


✍️ DefineEasy is a simple and powerful Android app that leverages Jetpack Compose, MVVM architecture, Dagger Hilt, and Room Database to provide users with quick and easy word definitions. With DefineEasy, users can search for words, view detailed definitions, and improve their vocabulary effortlessly.

Download
| Android | 
|:-:|

[<img src="https://github.com/rudradave1/DefineEasy/assets/35660907/14acc868-63f6-452e-bf20-671deeb1c4b2" height="50">](https://play.google.com/store/apps/details?id=com.rudra.defineeasy) 

<h1 align=center>Features</h1>

• Search: Users can easily search for words, and DefineEasy will provide comprehensive definitions.

• Spaced Repetition (SM-2): Master new vocabulary efficiently using the scientifically-backed SM-2 spaced repetition algorithm.

• Offline Access: Once you've searched for a word, you can access its definition offline.

• Intuitive UI: The app features a user-friendly interface built with Jetpack Compose for a seamless experience.

<h1 align=center>Recent Updates 🚀</h1>

• **Major Feature Release (v3.1.0):**
    - **Custom Collections:** Users can now create and manage their own personalized word sets.
    - **Offline Pronunciation:** Integrated Android TTS for word pronunciations when offline.
    - **Learning Analytics:** New "Progress" tab with stats for Mastered and Learning words.
    - **Spaced Repetition:** Improved SM-2 scheduling and review history tracking.
• **Crashlytics Stability:** Fixed random crashes and improved stability with updated Proguard rules (v3.0.8).
• **Model Preservation:** Ensured all DTOs and entities are preserved during obfuscation to prevent deserialization errors.
• **Advertising ID:** Corrected AD_ID permission configuration for better Play Store compliance.
• **Performance:** Internal optimizations and performance improvements.

<h1 align=center>Screenshots 📸</h1>


||||
|:----------------------------------------:|:-----------------------------------------:|:-----------------------------------------:|
| ![ss1-landscape](https://github.com/rudradave1/DefineEasy/assets/98882610/d37a6594-9461-4f2a-b4a5-ae29fe268499) | ![ss2-landscape](https://github.com/rudradave1/DefineEasy/assets/98882610/5ddee057-e238-48aa-b3a1-2ed0e350b5cc) | ![ss3-landscape](https://github.com/rudradave1/DefineEasy/assets/98882610/9093ba38-e5bc-45e9-96c5-edad6cab1011)


<h1 align=center>Tech Stack</h1>

• Jetpack Compose: DefineEasy utilizes the power of Jetpack Compose for a modern and interactive user interface.

• MVVM Architecture: The app is built using the Model-View-ViewModel architectural pattern for clear separation of concerns.

• Dagger Hilt: Dagger Hilt is used for dependency injection, ensuring a maintainable and testable codebase.

• SM-2 Algorithm: Implements the SuperMemo-2 algorithm for optimized flashcard scheduling and long-term retention.

• Room Database: Room is employed for efficient local data storage, enabling offline access to definitions.

<h1 align=center>Getting Started</h1>
To build and run DefineEasy on your machine, follow these steps:

1. Clone the repository:

2. bash
Copy code
git clone https://github.com/rudradave1/DefineEasy.git
Open the project in Android Studio.

3. Build and run the app on an emulator or physical device.

4. Dependencies
DefineEasy relies on several key dependencies to provide its functionality:

Jetpack Compose
Dagger Hilt
Room Database
Retrofit
Please refer to the build.gradle files for a complete list of dependencies.

## Firebase Setup

Crashlytics is configured for release builds. Replace `app/google-services.json` with your own Firebase project configuration before shipping to production.

<h1 align=center>Contributing</h1>
We welcome contributions from the open-source community. If you'd like to contribute to DefineEasy, please follow our contribution guidelines.

License
[MIT License](LICENSE)
<h1 align=center></h1>
Contact
If you have any questions or need assistance, feel free to reach out to the project maintainers:

GitHub Issues: [ISSUES](https://github.com/rudradave1/DefineEasy/issues) 
