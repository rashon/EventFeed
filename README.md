# EventFeed

EventFeed is an Android application designed to help users monitor the event feed provided by a web application. Built with modern Android development practices, it utilizes Jetpack Compose for the UI and follows a clean architecture pattern.

## 📱 Features

*   **Realtime event feed monitoring:** Live updates of events with auto-refresh and manual refresh controls.
*   **Event details:** Detailed event screen showing title, id and additional attributes, with offline handling.
*   **User profile:** Fetch and display user profile from the backend `/profile` endpoint.
*   **Connectivity awareness:** Offline indicator in the toolbar and graceful fallbacks when network requests fail.

## 🛠 Tech Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** [Jetpack Compose](https://developer.android.com/jetbrains/compose)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **State Management:** Kotlin Flows + StateFlow
*   **Design:** Material Design 3 (Material3)

## 🖥 Backend (Go)

A small Go server provides the API used by the app. The repo contains a Go module (`go.mod`) and a lightweight HTTP server exposing the main endpoints used by the Android client.

Quick overview:
*   Language: Go (module enabled)
*   Default port: `8080`
*   Endpoints:
    *   `GET /events` — returns a list of events (used by the event feed)
    *   `GET /profile` — returns the current user profile (used by the User Profile screen)

### Running locally

Prerequisites: Go 1.20\+ installed.

Build and run:
# bash
* cd backend   # or the folder that contains main/cmd
* go build -o eventserver ./cmd/server
* ./eventserver
# or run directly:

* go run ./cmd/server

# Docker
* Build and run with Docker (if a Dockerfile is present):
* docker build -t eventserver:local .
* docker run -p 8080:8080 eventserver:local


## Development notes
* The Go server is intentionally lightweight to make local testing easy.
* If you change the server address/port, update the Android client baseUrl in NetworkModule (see installation step 3) to point to the running backend (for example http://10.0.2.2:8080 for the emulator).

## 🚀 Getting Started

### Prerequisites

*   Android Studio Hedgehog or newer.
*   JDK 17 or newer.

### Installation

1.  **Clone the repository**
2.  **Open in Android Studio**
3.  **Update the backend base URL**  
       Locate the `NetworkModule` used by your DI setup and update the base URL to point to your backend server.
4.  **Build the project:**

### License

...