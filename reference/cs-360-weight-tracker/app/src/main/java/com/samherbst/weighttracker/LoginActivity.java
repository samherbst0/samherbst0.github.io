package com.samherbst.weighttracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText usernameInput, passwordInput;
    private MaterialButton loginButton, createAccountButton;

    // Database
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Find views by ID
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set click listeners
        loginButton.setOnClickListener(v -> handleLogin());
        createAccountButton.setOnClickListener(v -> handleCreateAccount());
    }

    /**
     * Handle login button click
     * Validates credentials against database
     */
    private void handleLogin() {
        // Get input values
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate fields are not empty
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check credentials in database
        if (dbHelper.validateUser(username, password)) {
            // Success - get user ID and save session
            int userId = dbHelper.getUserId(username);
            saveUserSession(userId, username);

            // Show success message
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();

            // Navigate to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish(); // Close login screen
        } else {
            // Failed - show error
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle create account button click
     * Adds new user to database
     */
    private void handleCreateAccount() {
        // Get input values
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate fields are not empty
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to add user to database
        if (dbHelper.addUser(username, password)) {
            // Success
            Toast.makeText(this, R.string.account_created, Toast.LENGTH_LONG).show();

            // Clear fields so user can login
            usernameInput.setText("");
            passwordInput.setText("");

            // Focus on username field
            usernameInput.requestFocus();
        } else {
            // Failed - username already exists
            Toast.makeText(this, R.string.account_exists, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save user session in SharedPreferences
     * Allows app to remember logged-in user
     */
    private void saveUserSession(int userId, String username) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("USER_ID", userId);
        editor.putString("USERNAME", username);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();
    }
}