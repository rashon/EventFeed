# PriceTracker

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
*   **State Management:**
*   **Design:** Material Design 3 (Material3)


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