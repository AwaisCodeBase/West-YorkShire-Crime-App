package com.uni.crimes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.uni.crimes.auth.AuthManager;
import com.uni.crimes.model.User;

/**
 * Login Fragment - Authentication Functionality
 * Allows registered users to authenticate using email and password
 * Implements proper input validation and error handling
 * 
 * Functional Description:
 * - Allows registered users to authenticate using email and password
 * - Input validation ensures email format is valid and password field not empty
 * - Upon successful authentication:
 *   * Normal users redirected to Home Screen
 *   * Admin users redirected to Home Screen with admin privileges
 * - Authentication state preserved for persistent login sessions
 * - Error handling displays meaningful messages for invalid credentials
 * - Fulfills coursework requirement for user authentication
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private MaterialTextView tvRegister, tvError, tvDemoCredentials;
    private AuthManager authManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - Fragment lifecycle method");
        authManager = new AuthManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called - Fragment lifecycle method");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called - Setting up authentication functionality");
        
        initViews(view);
        setupListeners();
        setupDemoCredentials();
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvRegister = view.findViewById(R.id.tv_register);
        tvError = view.findViewById(R.id.tv_error);
        tvDemoCredentials = view.findViewById(R.id.tv_demo_credentials);
    }

    /**
     * Setup demo credentials display for coursework demonstration
     */
    private void setupDemoCredentials() {
        if (tvDemoCredentials != null) {
            tvDemoCredentials.setText("Demo Credentials:\nAdmin: admin@crimes.com / admin123\nUser: user@crimes.com / user123");
            tvDemoCredentials.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setup input validation and button listeners
     */
    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(v -> performLogin());
        
        tvRegister.setOnClickListener(v -> {
            Log.d(TAG, "Navigate to Registration Screen");
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).switchToFragment(new RegistrationFragment());
            }
        });
    }

    /**
     * Validate user input as per coursework requirements
     * Ensures email format is valid and password field is not empty
     */
    private void validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        boolean isValid = !email.isEmpty() && !password.isEmpty() && 
                         android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        
        btnLogin.setEnabled(isValid);
    }

    /**
     * Perform user authentication
     * Implements proper error handling and role-based redirection
     */
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        Log.d(TAG, "Attempting login for email: " + email);
        
        hideError();
        btnLogin.setEnabled(false);
        
        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Login successful for user: " + user.getFullName() + ", Role: " + user.getRole());
                Toast.makeText(getContext(), "Welcome " + user.getFullName(), Toast.LENGTH_SHORT).show();
                
                // Redirect based on user role (both Admin and Normal users go to MainActivity)
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
                
                Log.d(TAG, "User redirected to Home Screen with role: " + user.getRole());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Login failed: " + error);
                showError(error);
                btnLogin.setEnabled(true);
            }
        });
    }

    /**
     * Display error message to user
     */
    private void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide error message
     */
    private void hideError() {
        if (tvError != null) {
            tvError.setVisibility(View.GONE);
        }
    }

    // Lifecycle methods with explicit logging as per coursework requirements

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called - Fragment lifecycle method");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called - Fragment lifecycle method");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called - Fragment lifecycle method");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called - Fragment lifecycle method");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Fragment lifecycle method");
    }
}