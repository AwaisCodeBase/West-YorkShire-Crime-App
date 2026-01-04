# West Yorkshire Crimes Android Application
## Technical Documentation

---

## Table of Contents

1. [Group Contribution](#1-group-contribution)
   - 1.1 Elliot Gibson – 20006474
   - 1.2 Natalia Lewandowska – 20015091
   - 1.3 David Vital – 20008506
2. [Introduction](#2-introduction)
3. [Case Study Brief](#3-case-study-brief)
4. [Problem Definition and Analysis](#4-problem-definition-and-analysis)
5. [Requirements](#5-requirements)
   - 5.1 Mandatory Requirements
   - 5.2 Optional Requirements
6. [Design and MVC](#6-design-and-mvc)
   - 6.1 Design
   - 6.2 MVC vs MVVM
7. [Implementation](#7-implementation)
   - 7.1 Login and Sign Up Activities
   - 7.2 Main Activity
   - 7.3 Found List Fragment
   - 7.4 Form Fragment
   - 7.5 Firebase Database
8. [Testing](#8-testing)
   - 8.1 Functionality Testing
   - 8.2 Usability Testing/Surveying
9. [Conclusions](#9-conclusions)
10. [Bibliography](#10-bibliography)
11. [Appendices](#11-appendices)

---

## 1. Group Contribution

### 1.1 Elliot Gibson – 20006474
[To be completed by team member]

### 1.2 Natalia Lewandowska – 20015091
[To be completed by team member]

### 1.3 David Vital – 20008506
[To be completed by team member]

---

## 2. Introduction

The West Yorkshire Crimes application is a native Android mobile application developed to provide users with access to crime data from the West Yorkshire region. The application enables users to view, search, and analyze crime records through an intuitive mobile interface with map visualization capabilities.

The application is built using modern Android development practices, implementing the MVVM (Model-View-ViewModel) architectural pattern with Room database for local data persistence. It features role-based access control, distinguishing between regular users and administrators, with administrators having additional capabilities for data management.

### Key Features:
- User authentication with role-based access control (Admin/User)
- Crime data visualization in list and map formats
- Advanced search functionality across multiple fields
- CRUD operations for crime records (Admin only)
- CSV data import functionality
- Mapbox integration for geographic visualization
- Offline-capable local SQLite database using Room

### Technology Stack:
- **Language:** Java
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 36
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite)
- **Maps:** Mapbox SDK v11+
- **UI Components:** Material Design Components
- **Navigation:** Single Activity with Fragment-based navigation

---

## 3. Case Study Brief

The West Yorkshire Police department requires a mobile application to make crime data accessible to the public and authorized personnel. The application must:

1. **Display Crime Data:** Present crime records from the Yorkshire region in an accessible format
2. **Enable Search:** Allow users to search and filter crime data by various criteria
3. **Geographic Visualization:** Show crime locations on an interactive map
4. **User Management:** Support different user roles with appropriate access levels
5. **Data Management:** Allow administrators to import, add, update, and delete crime records

The crime dataset includes the following information:
- Crime ID (unique identifier)
- Crime Type (e.g., Burglary, Vehicle crime, Anti-social behaviour)
- Reported By (police force)
- LSOA Name (Lower Layer Super Output Area)
- Geographic coordinates (Latitude, Longitude)
- Outcome Category
- Month of occurrence

---

## 4. Problem Definition and Analysis

### Problem Statement
Citizens and law enforcement personnel need a convenient way to access and analyze crime data for the West Yorkshire region. Traditional methods of accessing this data through websites or reports are cumbersome and do not provide real-time geographic context.

### Analysis

#### User Needs:
1. **General Public:** Need to view crime statistics in their area for safety awareness
2. **Administrators:** Need to manage and update crime records efficiently
3. **All Users:** Need intuitive search and filtering capabilities

#### Technical Challenges:
1. **Data Volume:** The crime dataset contains numerous records requiring efficient storage and retrieval
2. **Geographic Data:** Coordinates must be accurately displayed on maps
3. **Offline Access:** Users should be able to access data without constant internet connectivity
4. **Security:** Role-based access must be enforced to protect administrative functions

#### Solution Approach:
- Implement local SQLite database using Room for offline capability and performance
- Use MVVM architecture for clean separation of concerns and testability
- Integrate Mapbox for professional-grade map visualization
- Implement SharedPreferences-based authentication with role management
- Design intuitive UI following Material Design guidelines

---

## 5. Requirements

### 5.1 Mandatory Requirements

| ID | Requirement | Implementation Status |
|----|-------------|----------------------|
| MR1 | User authentication (Login/Registration) | ✅ Implemented |
| MR2 | Display all crime records from database | ✅ Implemented |
| MR3 | Search crimes by any field | ✅ Implemented |
| MR4 | Search crimes by specific field | ✅ Implemented |
| MR5 | View crime details | ✅ Implemented |
| MR6 | Role-based access control (Admin/User) | ✅ Implemented |
| MR7 | Admin: Add new crime records | ✅ Implemented |
| MR8 | Admin: Update existing crime records | ✅ Implemented |
| MR9 | Admin: Delete crime records | ✅ Implemented |
| MR10 | Persistent login sessions | ✅ Implemented |
| MR11 | Logout functionality | ✅ Implemented |
| MR12 | Activity/Fragment lifecycle logging | ✅ Implemented |

### 5.2 Optional Requirements

| ID | Requirement | Implementation Status |
|----|-------------|----------------------|
| OR1 | Map visualization of crime locations | ✅ Implemented |
| OR2 | CSV data import functionality | ✅ Implemented |
| OR3 | Swipe-to-refresh for data updates | ✅ Implemented |
| OR4 | Show search results on map | ✅ Implemented |
| OR5 | Material Design UI components | ✅ Implemented |
| OR6 | Splash screen with authentication check | ✅ Implemented |
| OR7 | Admin dashboard with statistics | ✅ Implemented |
| OR8 | Input validation on all forms | ✅ Implemented |

---

## 6. Design and MVC

### 6.1 Design

#### Application Architecture

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Activities │  │  Fragments  │  │      Adapters       │  │
│  │ - Splash    │  │ - Login     │  │ - CrimeAdapter      │  │
│  │ - Login     │  │ - Register  │  │                     │  │
│  │ - Main      │  │ - CrimesList│  │                     │  │
│  │             │  │ - Search    │  │                     │  │
│  │             │  │ - Map       │  │                     │  │
│  │             │  │ - Admin     │  │                     │  │
│  │             │  │ - Detail    │  │                     │  │
│  │             │  │ - AddUpdate │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                         │
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │   CrimeViewModel    │  │      AuthViewModel          │   │
│  │ - LiveData<Crimes>  │  │ - Authentication state      │   │
│  │ - Search operations │  │ - User session management   │   │
│  │ - CRUD operations   │  │                             │   │
│  └─────────────────────┘  └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                        │
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │   CrimeRepository   │  │      AuthRepository         │   │
│  │ - Data abstraction  │  │ - Auth operations           │   │
│  │ - Business logic    │  │ - Session management        │   │
│  └─────────────────────┘  └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                            │
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │   Room Database     │  │    SharedPreferences        │   │
│  │ - CrimeDatabase     │  │ - User session data         │   │
│  │ - CrimeDao          │  │ - Authentication state      │   │
│  │ - Crime Entity      │  │                             │   │
│  └─────────────────────┘  └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### Screen Flow Diagram

```
┌──────────────┐
│    Splash    │
│   Activity   │
└──────┬───────┘
       │
       ▼
   ┌───────────────┐
   │ Auth Check    │
   └───────┬───────┘
           │
     ┌─────┴─────┐
     │           │
     ▼           ▼
┌─────────┐  ┌─────────────┐
│ Login   │  │    Main     │
│Activity │  │  Activity   │
└────┬────┘  └──────┬──────┘
     │              │
     ▼              ▼
┌─────────┐  ┌─────────────────────────────────────┐
│ Login   │  │         Bottom Navigation           │
│Fragment │  │  ┌───────┬───────┬───────┬───────┐ │
└────┬────┘  │  │Crimes │Search │  Map  │ Admin │ │
     │       │  │ List  │       │       │(Admin)│ │
     ▼       │  └───┬───┴───┬───┴───┬───┴───┬───┘ │
┌─────────┐  │      │       │       │       │     │
│Register │  │      ▼       ▼       ▼       ▼     │
│Fragment │  │  ┌───────┐ ┌─────┐ ┌─────┐ ┌─────┐ │
└─────────┘  │  │Detail │ │Map  │ │Map  │ │Add/ │ │
             │  │       │ │View │ │View │ │Edit │ │
             │  └───────┘ └─────┘ └─────┘ └─────┘ │
             └─────────────────────────────────────┘
```

#### Database Schema

```
┌─────────────────────────────────────────┐
│              crimes (Table)              │
├─────────────────────────────────────────┤
│ crimeId (PK)      │ TEXT NOT NULL       │
│ crimeType         │ TEXT                │
│ reportedBy        │ TEXT                │
│ lsoaName          │ TEXT                │
│ latitude          │ REAL                │
│ longitude         │ REAL                │
│ outcomeCategory   │ TEXT                │
│ month             │ TEXT                │
└─────────────────────────────────────────┘
```

### 6.2 MVC vs MVVM

#### Why MVVM was Chosen Over MVC

The application implements the **MVVM (Model-View-ViewModel)** architectural pattern instead of traditional MVC. Here's a comparison:

| Aspect | MVC | MVVM (Chosen) |
|--------|-----|---------------|
| **Data Binding** | Manual UI updates | LiveData automatic updates |
| **Testability** | Difficult to unit test | ViewModels easily testable |
| **Lifecycle Awareness** | Manual handling | Built-in lifecycle awareness |
| **Code Separation** | Controller can become bloated | Clear separation of concerns |
| **Android Compatibility** | Not optimized for Android | Designed for Android |

#### MVVM Components in This Application

**Model:**
- `Crime.java` - Entity class representing crime data
- `User.java` - Entity class representing user data
- `CrimeDatabase.java` - Room database instance
- `CrimeDao.java` - Data Access Object for database operations

**View:**
- Activities: `SplashActivity`, `LoginActivity`, `MainActivity`
- Fragments: `LoginFragment`, `RegistrationFragment`, `CrimesListFragment`, `SearchFragment`, `MapFragment`, `AdminFragment`, `CrimeDetailFragment`, `AddUpdateCrimeFragment`
- Layouts: XML layout files defining UI structure

**ViewModel:**
- `CrimeViewModel.java` - Manages crime-related UI data
- `AuthViewModel.java` - Manages authentication state

**Repository:**
- `CrimeRepository.java` - Abstracts data sources for crime data
- `AuthRepository.java` - Handles authentication operations

#### Benefits Realized

1. **Lifecycle Management:** ViewModels survive configuration changes (screen rotation)
2. **Reactive UI:** LiveData automatically updates UI when data changes
3. **Separation of Concerns:** Business logic isolated from UI code
4. **Testability:** ViewModels can be unit tested without Android framework
5. **Maintainability:** Clear boundaries between components

---

## 7. Implementation

### 7.1 Login and Sign Up Activities

#### LoginActivity
The `LoginActivity` serves as the container for authentication-related fragments. It implements a single-activity pattern for the authentication flow.

```java
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }

    public void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
```

#### LoginFragment
The `LoginFragment` handles user authentication with the following features:

**Key Features:**
- Email and password input validation
- Real-time input validation using TextWatcher
- Demo credentials display for testing
- Error message handling
- Role-based redirection after successful login

**Authentication Flow:**
```
User Input → Validation → AuthManager.login() → 
    ├── Success → Save Session → Navigate to MainActivity
    └── Failure → Display Error Message
```

**Input Validation:**
```java
private void validateInputs() {
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString().trim();
    
    boolean isValid = !email.isEmpty() && !password.isEmpty() && 
                     android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    
    btnLogin.setEnabled(isValid);
}
```

#### RegistrationFragment
The `RegistrationFragment` allows new users to create accounts with:
- Full name, email, password, and confirm password fields
- Password strength validation (minimum 6 characters)
- Password match confirmation
- Automatic login after successful registration

**Registration Validation:**
```java
private void validateInputs() {
    boolean isValid = !fullName.isEmpty() && 
                     !email.isEmpty() && 
                     android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                     password.length() >= 6 &&
                     password.equals(confirmPassword);
    
    btnRegister.setEnabled(isValid);
}
```

#### AuthManager
The `AuthManager` class handles all authentication operations:

```java
public class AuthManager {
    // Demo credentials
    private static final String ADMIN_EMAIL = "admin@crimes.com";
    private static final String ADMIN_PASSWORD = "admin123";
    
    public void login(String email, String password, AuthCallback callback) {
        // Check for admin credentials
        if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
            User adminUser = new User("admin_uid", "Admin User", email, "Admin");
            saveUserToPrefs(adminUser);
            callback.onSuccess(adminUser);
            return;
        }
        // ... additional authentication logic
    }
}
```

### 7.2 Main Activity

The `MainActivity` serves as the main navigation hub implementing:

**Single Activity Architecture:**
- Uses `FrameLayout` as fragment container
- `BottomNavigationView` for primary navigation
- `Toolbar` with logout option

**Role-Based Navigation:**
```java
private void setupBottomNavigation() {
    Menu menu = bottomNav.getMenu();
    MenuItem adminItem = menu.findItem(R.id.nav_admin);
    if (adminItem != null) {
        String userRole = authManager.getCurrentUserRole();
        boolean isAdmin = "Admin".equals(userRole);
        adminItem.setVisible(isAdmin);
    }
    bottomNav.setOnItemSelectedListener(navListener);
}
```

**Navigation Listener:**
```java
private final BottomNavigationView.OnItemSelectedListener navListener =
    item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_crimes_list) {
            selectedFragment = new CrimesListFragment();
        } else if (itemId == R.id.nav_search) {
            selectedFragment = new SearchFragment();
        } else if (itemId == R.id.nav_map) {
            selectedFragment = new MapFragment();
        } else if (itemId == R.id.nav_admin) {
            if ("Admin".equals(authManager.getCurrentUserRole())) {
                selectedFragment = new AdminFragment();
            } else {
                return false; // Access denied
            }
        }
        // ... fragment transaction
        return true;
    };
```

**Logout Implementation:**
```java
private void performLogout() {
    authManager.logout();
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### 7.3 Found List Fragment (CrimesListFragment)

The `CrimesListFragment` displays all crime records using RecyclerView with MVVM pattern.

**Key Components:**
- `RecyclerView` with `LinearLayoutManager`
- `SwipeRefreshLayout` for pull-to-refresh
- `CrimeAdapter` for data binding
- `CrimeViewModel` for data management

**ViewModel Observation:**
```java
private void observeViewModel() {
    crimeViewModel.getAllCrimes().observe(getViewLifecycleOwner(), crimes -> {
        if (crimes != null) {
            adapter.setCrimes(crimes);
            swipeRefresh.setRefreshing(false);
            Log.d(TAG, "Crime list updated. Count: " + crimes.size());
        }
    });

    crimeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        if (isLoading != null) {
            swipeRefresh.setRefreshing(isLoading);
        }
    });
}
```

**CrimeAdapter Implementation:**
```java
public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder> {
    
    public interface OnCrimeClickListener {
        void onCrimeClick(Crime crime);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CrimeViewHolder holder, int position) {
        Crime crime = crimes.get(position);
        holder.bind(crime);
    }
    
    class CrimeViewHolder extends RecyclerView.ViewHolder {
        public void bind(Crime crime) {
            tvCrimeType.setText(crime.getCrimeType());
            tvLsoaName.setText(crime.getLsoaName());
            tvOutcome.setText(crime.getOutcome());
        }
    }
}
```

### 7.4 Form Fragment (AddUpdateCrimeFragment)

The `AddUpdateCrimeFragment` handles both adding new crimes and updating existing ones.

**Dual Mode Operation:**
```java
public static AddUpdateCrimeFragment newInstance(String crimeId) {
    AddUpdateCrimeFragment fragment = new AddUpdateCrimeFragment();
    Bundle args = new Bundle();
    if (crimeId != null) {
        args.putString(ARG_CRIME_ID, crimeId);
    }
    fragment.setArguments(args);
    return fragment;
}

@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
        crimeId = getArguments().getString(ARG_CRIME_ID);
        isEditMode = crimeId != null;
    }
}
```

**Form Fields:**
- Crime ID (auto-generated or existing)
- Crime Type
- Reported By
- LSOA Name
- Latitude/Longitude (with validation)
- Outcome Category
- Month

**Input Validation:**
```java
private void validateInputs() {
    boolean isValid = !crimeIdText.isEmpty() && 
                     !crimeType.isEmpty() && 
                     !reportedBy.isEmpty() && 
                     !lsoaName.isEmpty() && 
                     !latitudeText.isEmpty() && 
                     !longitudeText.isEmpty();
    
    // Validate coordinates
    if (isValid) {
        try {
            double lat = Double.parseDouble(latitudeText);
            double lng = Double.parseDouble(longitudeText);
            isValid = lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
        } catch (NumberFormatException e) {
            isValid = false;
        }
    }
    
    btnSave.setEnabled(isValid);
}
```

**Save Operation:**
```java
private void saveCrime() {
    Crime crime = new Crime(crimeIdText, crimeType, reportedBy, lsoaName, 
                          latitude, longitude, outcome, month);
    
    executor.execute(() -> {
        if (isEditMode) {
            database.crimeDao().updateCrime(crime);
        } else {
            database.crimeDao().insertCrime(crime);
        }
        // UI update on main thread
    });
}
```

### 7.5 Firebase Database

**Note:** The current implementation uses local SQLite database via Room instead of Firebase. This decision was made for:
- Offline-first capability
- Faster data access
- No external dependencies for core functionality
- Simplified deployment

#### Room Database Implementation

**CrimeDatabase:**
```java
@Database(entities = {Crime.class}, version = 2, exportSchema = false)
public abstract class CrimeDatabase extends RoomDatabase {
    
    private static CrimeDatabase INSTANCE;
    
    public abstract CrimeDao crimeDao();
    
    public static synchronized CrimeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    CrimeDatabase.class, "crime_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
```

**CrimeDao (Data Access Object):**
```java
@Dao
public interface CrimeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrime(Crime crime);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCrimes(List<Crime> crimes);
    
    @Query("SELECT * FROM crimes ORDER BY crimeId DESC")
    LiveData<List<Crime>> getAllCrimes();
    
    @Query("SELECT * FROM crimes WHERE " +
           "crimeType LIKE '%' || :searchTerm || '%' OR " +
           "lsoaName LIKE '%' || :searchTerm || '%' OR " +
           "outcomeCategory LIKE '%' || :searchTerm || '%' OR " +
           "reportedBy LIKE '%' || :searchTerm || '%'")
    LiveData<List<Crime>> searchCrimesByAnyField(String searchTerm);
    
    @Update
    void updateCrime(Crime crime);
    
    @Delete
    void deleteCrime(Crime crime);
    
    @Query("SELECT COUNT(*) FROM crimes")
    LiveData<Integer> getCrimeCountLive();
}
```

**Crime Entity:**
```java
@Entity(tableName = "crimes")
public class Crime {
    @PrimaryKey
    @NonNull
    private String crimeId;
    private String crimeType;
    private String reportedBy;
    private String lsoaName;
    private double latitude;
    private double longitude;
    private String outcomeCategory;
    private String month;
    
    // Constructors, getters, setters...
}
```

---

## 8. Testing

### 8.1 Functionality Testing

#### Test Cases

| Test ID | Test Case | Steps | Expected Result | Status |
|---------|-----------|-------|-----------------|--------|
| TC001 | User Login - Valid Credentials | 1. Enter admin@crimes.com 2. Enter admin123 3. Click Login | User logged in, redirected to Main | ✅ Pass |
| TC002 | User Login - Invalid Credentials | 1. Enter wrong@email.com 2. Enter wrongpass 3. Click Login | Error message displayed | ✅ Pass |
| TC003 | User Registration | 1. Enter valid details 2. Click Register | Account created, auto-login | ✅ Pass |
| TC004 | View Crime List | 1. Login 2. Navigate to Crimes tab | All crimes displayed in list | ✅ Pass |
| TC005 | Search - All Fields | 1. Enter search term 2. Select "All Fields" 3. Click Search | Matching crimes displayed | ✅ Pass |
| TC006 | Search - Specific Field | 1. Enter search term 2. Select specific field 3. Click Search | Filtered results displayed | ✅ Pass |
| TC007 | View Crime Details | 1. Click on crime item | Crime details screen shown | ✅ Pass |
| TC008 | Map View | 1. Navigate to Map tab | Map with crime markers displayed | ✅ Pass |
| TC009 | Admin - Add Crime | 1. Login as admin 2. Navigate to Admin 3. Add new crime | Crime added to database | ✅ Pass |
| TC010 | Admin - Edit Crime | 1. Login as admin 2. View crime details 3. Click Edit 4. Modify and save | Crime updated | ✅ Pass |
| TC011 | Admin - Delete Crime | 1. Login as admin 2. View crime details 3. Click Delete | Crime removed from database | ✅ Pass |
| TC012 | CSV Import | 1. Login as admin 2. Navigate to Admin 3. Import CSV | Data imported successfully | ✅ Pass |
| TC013 | Logout | 1. Click logout in toolbar | Session cleared, redirected to login | ✅ Pass |
| TC014 | Persistent Session | 1. Login 2. Close app 3. Reopen app | User still logged in | ✅ Pass |
| TC015 | Role-Based Access | 1. Login as regular user 2. Check Admin tab | Admin tab not visible | ✅ Pass |

#### Lifecycle Testing

All Activities and Fragments implement lifecycle logging:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate() called - Activity lifecycle method");
}

@Override
protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart() called - Activity lifecycle method");
}

@Override
protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume() called - Activity lifecycle method");
}

@Override
protected void onPause() {
    super.onPause();
    Log.d(TAG, "onPause() called - Activity lifecycle method");
}

@Override
protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop() called - Activity lifecycle method");
}

@Override
protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy() called - Activity lifecycle method");
}
```

### 8.2 Usability Testing/Surveying

#### Usability Test Plan

**Participants:** [Number] users with varying technical backgrounds

**Tasks:**
1. Register a new account
2. Login with existing credentials
3. Find crimes in a specific area
4. View crime details
5. (Admin) Add a new crime record
6. (Admin) Edit an existing crime
7. View crimes on the map
8. Logout

#### Survey Questions

1. How easy was it to navigate the application? (1-5 scale)
2. Was the crime information clearly presented? (1-5 scale)
3. How useful did you find the search functionality? (1-5 scale)
4. Was the map visualization helpful? (1-5 scale)
5. Did you encounter any errors or issues? (Yes/No + description)
6. What features would you like to see added?
7. Overall satisfaction with the application (1-5 scale)

#### Survey Results

[To be completed after user testing]

| Question | Average Score |
|----------|---------------|
| Navigation ease | /5 |
| Information clarity | /5 |
| Search usefulness | /5 |
| Map helpfulness | /5 |
| Overall satisfaction | /5 |

---

## 9. Conclusions

The West Yorkshire Crimes Android application successfully meets all mandatory requirements and implements several optional features. Key achievements include:

### Accomplishments:
1. **Robust Authentication System:** Implemented role-based access control with persistent sessions
2. **Efficient Data Management:** Room database provides fast, offline-capable data access
3. **Intuitive User Interface:** Material Design components ensure modern, accessible UI
4. **Geographic Visualization:** Mapbox integration enables crime location mapping
5. **MVVM Architecture:** Clean separation of concerns improves maintainability and testability
6. **Comprehensive CRUD Operations:** Full data management capabilities for administrators

### Challenges Overcome:
1. Implementing efficient search across multiple database fields
2. Managing fragment lifecycle with proper data persistence
3. Integrating Mapbox SDK with crime coordinate data
4. Balancing offline capability with data freshness

### Future Enhancements:
1. Cloud synchronization for multi-device access
2. Push notifications for crime alerts
3. Statistical analysis and crime trend visualization
4. User location-based crime proximity alerts
5. Export functionality for crime reports

---

## 10. Bibliography

1. Android Developers. (2024). "Guide to App Architecture." https://developer.android.com/topic/architecture

2. Android Developers. (2024). "Room Persistence Library." https://developer.android.com/training/data-storage/room

3. Android Developers. (2024). "ViewModel Overview." https://developer.android.com/topic/libraries/architecture/viewmodel

4. Android Developers. (2024). "LiveData Overview." https://developer.android.com/topic/libraries/architecture/livedata

5. Mapbox. (2024). "Mapbox Maps SDK for Android." https://docs.mapbox.com/android/maps/guides/

6. Material Design. (2024). "Material Components for Android." https://material.io/develop/android

7. Google. (2024). "Android Jetpack." https://developer.android.com/jetpack

8. West Yorkshire Police. (2024). "Crime Data." https://data.police.uk/

---

## 11. Appendices

### Appendix A: Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/uni/crimes/
│       │   ├── adapter/
│       │   │   └── CrimeAdapter.java
│       │   ├── auth/
│       │   │   └── AuthManager.java
│       │   ├── config/
│       │   │   ├── DatabaseConfig.java
│       │   │   └── MapboxConfig.java
│       │   ├── database/
│       │   │   ├── CrimeDao.java
│       │   │   └── CrimeDatabase.java
│       │   ├── model/
│       │   │   ├── Crime.java
│       │   │   └── User.java
│       │   ├── network/
│       │   │   ├── ApiService.java
│       │   │   └── NetworkModule.java
│       │   ├── repository/
│       │   │   ├── AuthRepository.java
│       │   │   ├── CrimeRepository.java
│       │   │   └── HybridCrimeRepository.java
│       │   ├── utils/
│       │   │   ├── CSVImporter.java
│       │   │   ├── ErrorHandler.java
│       │   │   └── ValidationUtils.java
│       │   ├── viewmodel/
│       │   │   ├── AuthViewModel.java
│       │   │   └── CrimeViewModel.java
│       │   ├── AddUpdateCrimeFragment.java
│       │   ├── AdminFragment.java
│       │   ├── CrimeDetailFragment.java
│       │   ├── CrimesApplication.java
│       │   ├── CrimesListFragment.java
│       │   ├── ImportDatasetFragment.java
│       │   ├── LoginActivity.java
│       │   ├── LoginFragment.java
│       │   ├── MainActivity.java
│       │   ├── MapFragment.java
│       │   ├── RegistrationFragment.java
│       │   ├── SearchFragment.java
│       │   └── SplashActivity.java
│       ├── res/
│       │   ├── layout/
│       │   ├── menu/
│       │   ├── values/
│       │   └── drawable/
│       ├── assets/
│       │   └── crimeyorkshire.csv
│       └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

### Appendix B: Dependencies

```kotlin
dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    
    // Mapbox
    implementation(libs.mapbox.android.sdk)
    
    // Network (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

### Appendix C: Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@crimes.com | admin123 |
| User | user@crimes.com | user123 |

### Appendix D: Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

---

*Document Version: 1.0*
*Last Updated: January 2026*
