## System Requirements & Environment

To run SubtitleFlow locally, ensure your system has the following dependencies installed and accessible via your system `PATH`:

1. **Java Development Kit (JDK):** Version 17 or higher (configured for Java 21/26).

2. **Apache Maven:** Build and dependency management tool (version 3.8+).

3. **FFmpeg:** Required for video processing, scaling, and subtitle burn-in.

4. **OpenAI Whisper (CLI):** Local AI speech recognition tool (`pip install openai-whisper`).

5. **yt-dlp:** Command-line video downloader tool.



## Setup and Launch Instructions



1. **Open the Project:**

   Open the `SubtitleFlow` project folder in your Java IDE (e.g., IntelliJ IDEA).



2. **Compile and Build Dependencies:**

   Open your terminal in the root directory of the project (where `pom.xml` is located) and run:

   ```bash

   mvn clean compile

   ```



3. **Run the Application:**

   Locate and run the main class: `org.example.Main`.

   Alternatively, run it via Maven:

   ```bash

   mvn exec:java -Dexec.mainClass="org.example.Main"

   ```



4. **Access the Web Interface:**

   Once the server starts successfully, open your web browser and navigate to:

   `http://localhost:8080`



## Sample Test Links for Verification

You can use the following public video links during your testing and presentation:

- **Test Video Link:** `https://www.youtube.com/watch?v=UTK3jisN7mY`

- **Alternative Test:** Any short public YouTube, TikTok, or Instagram video URL.



## Implemented Design Patterns

1. **Builder Pattern:** `SubtitleConfigBuilder` for configuring font color and size.

2. **Factory Method Pattern:** `DownloaderFactory` and `QualityStrategyFactory` for dynamic object creation.

3. **Facade Pattern:** `VideoProcessingFacade` to simplify subsystem interactions.

4. **Adapter Pattern:** `ConsoleCommandAdapter` for safe execution of system shell commands (`ProcessBuilder`).

5. **Observer Pattern:** `NotificationManager` for session event tracking.

6. **Strategy Pattern:** `VideoQualityStrategy` hierarchy for interchangeable video rendering qualities (360p, 720p, Best).