package com.samherbst.weighttracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements WeightAdapter.OnItemClickListener {

    // Constants
    private static final int SMS_PERMISSION_CODE = 100;

    // UI Components
    private TextView currentWeightText, goalWeightText;
    private MaterialButton setGoalButton;
    private RecyclerView weightRecyclerView;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton addWeightFab;

    // Data
    private DatabaseHelper dbHelper;
    private WeightAdapter adapter;
    private int userId;
    private String username;
    private boolean smsPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get user info from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Check SMS permission
        checkSmsPermission();

        // Load data
        loadWeightData();
        updateGoalDisplay();

        // Set click listeners
        setGoalButton.setOnClickListener(v -> showSetGoalDialog());
        addWeightFab.setOnClickListener(v -> showAddWeightDialog());
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        currentWeightText = findViewById(R.id.currentWeightText);
        goalWeightText = findViewById(R.id.goalWeightText);
        setGoalButton = findViewById(R.id.setGoalButton);
        weightRecyclerView = findViewById(R.id.weightRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        addWeightFab = findViewById(R.id.addWeightFab);

        // Setup RecyclerView
        weightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weightRecyclerView.setHasFixedSize(true);

        MaterialButton addWeightButton = findViewById(R.id.addWeightButton);
        addWeightButton.setOnClickListener(v -> showAddWeightDialog());
    }

    /**
     * Load weight data from database and display in grid
     */
    private void loadWeightData() {
        List<WeightEntry> weights = dbHelper.getAllWeights(userId);

        if (weights.isEmpty()) {
            // Show empty state
            weightRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            currentWeightText.setText("--");
        } else {
            // Show grid with data
            weightRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);

            // Create/update adapter
            if (adapter == null) {
                adapter = new WeightAdapter(weights, this);
                weightRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(weights);
            }

            // Update current weight display
            double latestWeight = dbHelper.getLatestWeight(userId);
            currentWeightText.setText(String.format(Locale.US, "%.1f", latestWeight));
        }
    }

    /**
     * Update goal weight display
     */
    private void updateGoalDisplay() {
        double goalWeight = dbHelper.getGoalWeight(userId);

        if (goalWeight > 0) {
            goalWeightText.setText(String.format(Locale.US, "%.1f", goalWeight));
            setGoalButton.setText(R.string.edit_goal);
        } else {
            goalWeightText.setText("--");
            setGoalButton.setText(R.string.set_goal);
        }
    }

    /**
     * Show dialog to add new weight entry
     */
    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_weight, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get dialog views
        TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextInputEditText dateInput = dialogView.findViewById(R.id.dateInput);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Set today's date as default
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        String todayDate = dateFormat.format(calendar.getTime());
        dateInput.setText(displayFormat.format(calendar.getTime()));

        // Date picker when clicking date field
        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        dateInput.setText(displayFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            String weightStr = weightInput.getText().toString().trim();

            if (TextUtils.isEmpty(weightStr)) {
                Toast.makeText(this, R.string.enter_valid_weight, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                String date = dateFormat.format(calendar.getTime());

                // Add to database
                if (dbHelper.addWeight(userId, date, weight)) {
                    Toast.makeText(this, R.string.weight_added, Toast.LENGTH_SHORT).show();

                    // Check if goal reached
                    checkGoalReached(weight);

                    // Refresh display
                    loadWeightData();
                    updateGoalDisplay();

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to add weight", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.enter_valid_weight, Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Show dialog to set/update goal weight
     */
    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextInputEditText goalWeightInput = dialogView.findViewById(R.id.goalWeightInput);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Pre-fill if goal exists
        double existingGoal = dbHelper.getGoalWeight(userId);
        if (existingGoal > 0) {
            dialogTitle.setText(R.string.update_goal_weight);
            goalWeightInput.setText(String.format(Locale.US, "%.1f", existingGoal));
        } else {
            dialogTitle.setText(R.string.set_goal_weight);
        }

        // Save button
        saveButton.setOnClickListener(v -> {
            String goalStr = goalWeightInput.getText().toString().trim();

            if (TextUtils.isEmpty(goalStr)) {
                Toast.makeText(this, R.string.enter_valid_weight, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double goalWeight = Double.parseDouble(goalStr);

                // Save to database
                if (dbHelper.setGoalWeight(userId, goalWeight)) {
                    if (existingGoal > 0) {
                        Toast.makeText(this, R.string.goal_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.goal_set, Toast.LENGTH_SHORT).show();
                    }

                    // Refresh display
                    updateGoalDisplay();

                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to set goal", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.enter_valid_weight, Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Handle delete button click from adapter
     */
    @Override
    public void onDeleteClick(WeightEntry entry) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (dbHelper.deleteWeight(entry.getId())) {
                        Toast.makeText(this, R.string.weight_deleted, Toast.LENGTH_SHORT).show();
                        loadWeightData();
                        updateGoalDisplay();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * Check if user has reached their goal weight
     * If yes, send SMS notification (if permission granted)
     */
    private void checkGoalReached(double newWeight) {
        double goalWeight = dbHelper.getGoalWeight(userId);

        // Check if goal is set and reached
        if (goalWeight > 0 && newWeight <= goalWeight) {
            // Goal reached!
            if (smsPermissionGranted) {
                sendGoalReachedSms();
            } else {
                // Show in-app notification
                showGoalReachedDialog();
            }
        }
    }

    /**
     * Show dialog congratulating user on reaching goal
     */
    private void showGoalReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Goal Reached!")
                .setMessage(R.string.goal_reached)
                .setPositiveButton("OK", null)
                .show();
    }

    // ========== SMS PERMISSION HANDLING ==========

    /**
     * Check if SMS permission is granted
     * If not, request it from user
     */
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsPermissionGranted = true;
        } else {
            // Request permission
            requestSmsPermission();
        }
    }

    /**
     * Request SMS permission from user
     */
    private void requestSmsPermission() {
        // Show explanation dialog first
        new AlertDialog.Builder(this)
                .setTitle(R.string.sms_permission_title)
                .setMessage(R.string.sms_permission_message)
                .setPositiveButton(R.string.allow, (dialog, which) -> {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            SMS_PERMISSION_CODE
                    );
                })
                .setNegativeButton(R.string.deny, (dialog, which) -> {
                    smsPermissionGranted = false;
                    Toast.makeText(this,
                            "SMS notifications disabled. You can enable them in Settings.",
                            Toast.LENGTH_LONG).show();
                })
                .show();
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionGranted = true;
                Toast.makeText(this, "SMS notifications enabled!", Toast.LENGTH_SHORT).show();
            } else {
                smsPermissionGranted = false;
                Toast.makeText(this,
                        "SMS notifications disabled. App will continue without notifications.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Send SMS notification when goal is reached
     */
    private void sendGoalReachedSms() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "Congratulations " + username + "! " +
                    "You've reached your goal weight! Keep up the great work!";

            String phoneNumber = "5559991234"; // Test number

            // LOG the SMS attempt
            android.util.Log.d("WeightTracker", "=== SMS NOTIFICATION ===");
            android.util.Log.d("WeightTracker", "To: " + phoneNumber);
            android.util.Log.d("WeightTracker", "Message: " + message);
            android.util.Log.d("WeightTracker", "SMS Permission: " + smsPermissionGranted);

            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            android.util.Log.d("WeightTracker", "SMS sent successfully!");

            Toast.makeText(this, R.string.sms_sent, Toast.LENGTH_SHORT).show();
            showGoalReachedDialog();

        } catch (Exception e) {
            android.util.Log.e("WeightTracker", "SMS Error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SMS notification", Toast.LENGTH_SHORT).show();
            showGoalReachedDialog();
        }
    }
}