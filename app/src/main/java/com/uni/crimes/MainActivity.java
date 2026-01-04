package com.uni.crimes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uni.crimes.auth.AuthManager;

/**
 * MainActivity - Main container with bottom navigation and toolbar
 * Implements single activity architecture with fragment containers
 * Provides role-based navigation and session management
 * 
 * Functional Description:
 * - Acts as main navigation hub using Bottom Navigation View
 * - Hosts multiple fragments using single activity architecture
 * - Dynamically adjusts visible options based on user role
 * - Admin-only features hidden from normal users (role-based access control)
 * - Logout option clears user session and redirects to Login Screen
 * - Demonstrates proper session management and navigation
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AuthManager authManager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - Activity lifecycle method");
        
        try {
            setContentView(R.layout.activity_main);

            authManager = new AuthManager(this);
            
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            bottomNav = findViewById(R.id.bottom_navigation);
            setupBottomNavigation();

            // Load default fragment (Crimes List)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrimesListFragment())
                    .commit();
                    
            Log.d(TAG, "MainActivity initialized successfully with role-based navigation");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    /**
     * Setup bottom navigation with role-based access control
     * Hides admin-only features from normal users
     */
    private void setupBottomNavigation() {
        try {
            // Implement role-based access control
            Menu menu = bottomNav.getMenu();
            MenuItem adminItem = menu.findItem(R.id.nav_admin);
            if (adminItem != null) {
                String userRole = authManager.getCurrentUserRole();
                boolean isAdmin = "Admin".equals(userRole);
                adminItem.setVisible(isAdmin);
                Log.d(TAG, "Role-based navigation setup - User role: " + userRole + ", Admin tab visible: " + isAdmin);
            }

            bottomNav.setOnItemSelectedListener(navListener);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    /**
     * Bottom navigation listener with role-based access control
     */
    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                try {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    
                    if (itemId == R.id.nav_crimes_list) {
                        selectedFragment = new CrimesListFragment();
                        Log.d(TAG, "Navigating to Crimes List Screen");
                    } else if (itemId == R.id.nav_search) {
                        selectedFragment = new SearchFragment();
                        Log.d(TAG, "Navigating to Search Crimes Screen");
                    } else if (itemId == R.id.nav_map) {
                        selectedFragment = new MapFragment();
                        Log.d(TAG, "Navigating to Crime Map Screen");
                    } else if (itemId == R.id.nav_admin) {
                        // Enforce role-based access control for Admin Dashboard
                        if ("Admin".equals(authManager.getCurrentUserRole())) {
                            selectedFragment = new AdminFragment();
                            Log.d(TAG, "Admin user navigating to Admin Dashboard");
                        } else {
                            Log.w(TAG, "Non-admin user attempted to access Admin Dashboard - access denied");
                            return false;
                        }
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error in navigation", e);
                    return false;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu", e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.action_logout) {
                performLogout();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in options item selected", e);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform logout operation
     * Clears user session and redirects to Login Screen
     * Demonstrates proper session management
     */
    private void performLogout() {
        try {
            Log.d(TAG, "Performing logout - clearing user session");
            authManager.logout();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Log.d(TAG, "User session cleared, redirected to Login Screen");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
        }
    }

    /**
     * Navigate to specific fragment (used by other fragments)
     * Enables navigation from Crime Detail, Add/Edit screens, etc.
     */
    public void navigateToFragment(Fragment fragment) {
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            Log.d(TAG, "Navigated to fragment: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to fragment", e);
        }
    }

    // Activity lifecycle methods with explicit logging as per coursework requirements

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
}