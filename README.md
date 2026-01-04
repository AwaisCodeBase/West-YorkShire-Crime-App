# 📱 West Yorkshire Crime Android Application

## 📌 Project Overview
The **West Yorkshire Crime Android Application** is a mobile application developed using **Java** and **Android Studio** as part of the *Mobile Application Development* coursework.  
The application provides a platform to **view, search, manage, and visualise crime data** for the West Yorkshire area using a structured dataset and an interactive user interface.

The system supports **both standard users and administrators**, ensuring secure access, efficient data handling, and clear visual representation of crime-related information.

---

## 🎯 Project Objectives
- Develop a user-friendly Android application for crime data exploration
- Implement secure user authentication
- Enable crime searching using multiple attributes
- Visualise crime locations using Google Maps
- Allow administrators to upload and manage crime data
- Apply proper Android architecture and lifecycle management

---

## 🛠️ Technologies Used
- **Programming Language:** Java  
- **IDE:** Android Studio  
- **Architecture Pattern:** MVC (Model–View–Controller)  
- **Database:** SQLite / Firebase  
- **Authentication:** Firebase Authentication  
- **Maps Integration:** Google Maps API  
- **Dataset:** West Yorkshire Crimes CSV (1612 records)

---

## ✨ Application Features

### 👤 User Features
- User registration and login
- View all crime records
- Search crimes by:
  - Crime type  
  - Location (LSOA)  
  - Outcome category  
- View crime locations on an interactive map

### 🛡️ Admin Features
- Secure admin login
- Upload crime data via CSV file
- Add, update, and delete crime records
- Manage persistent crime data storage

---

## 🧱 Application Architecture
The application follows the **MVC (Model–View–Controller)** architecture:

- **Model:** Crime and user data classes
- **View:** XML layouts, Activities, and Fragments
- **Controller:** Java classes handling business logic and user interaction

This approach improves maintainability, scalability, and code organisation.

---

## 📂 Project Structure
app/
┣ activities/
┃ ┣ LoginActivity.java
┃ ┣ RegisterActivity.java
┃ ┗ MainActivity.java
┣ fragments/
┃ ┣ CrimeListFragment.java
┃ ┣ CrimeFormFragment.java
┃ ┗ MapFragment.java
┣ models/
┃ ┗ Crime.java
┣ database/
┃ ┗ DatabaseHelper.java
┣ adapters/
┃ ┗ CrimeAdapter.java
┗ res/

---

## 🔄 Android Lifecycle Management
The application correctly implements Android lifecycle methods including:
- `onCreate()`
- `onStart()`
- `onResume()`
- `onPause()`
- `onDestroy()`

These ensure smooth navigation, efficient memory usage, and application stability.

---

## 🧪 Testing
The application was tested using:
- **Functionality testing:** Login, registration, search, CRUD operations, map display
- **Usability testing:** Interface clarity and ease of navigation
- **Data validation:** Ensuring accurate crime data handling

Testing screenshots are included in the coursework documentation.

---

## 📽️ Screencast Demonstration
A short screencast demonstrates:
- Application functionality
- Navigation flow
- Crime search and map features
- Code structure and architecture

📎 *The screencast is submitted separately as per coursework requirements.*

---

## 🚀 How to Run the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/West-Yorkshire-Crime-App.git
Open the project in Android Studio
Sync Gradle files
Run the application on:
Android Emulator, or
Physical Android device (USB debugging enabled)
📄 Dataset Information
The dataset used in this project contains 1612 crime records for the West Yorkshire area and was provided for academic use as part of the coursework.
⚠️ Academic Integrity
This project is original work developed solely for academic assessment.
All external tools, APIs, and libraries have been used responsibly and are acknowledged within the coursework report.
👨‍💻 Author
Awais Ali
BSc Computer Science
Mobile Application Development Coursework
