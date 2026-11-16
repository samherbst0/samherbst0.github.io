package com.samherbst.weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database info
    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 1;

    // TABLE 1: User logins
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // TABLE 2: Daily weights
    private static final String TABLE_WEIGHTS = "daily_weights";
    private static final String COL_WEIGHT_ID = "weight_id";
    private static final String COL_DATE = "date";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_USER_FK = "user_id";

    // TABLE 3: Goal weight
    private static final String TABLE_GOAL = "goal_weight";
    private static final String COL_GOAL_ID = "goal_id";
    private static final String COL_GOAL_WEIGHT = "goal_weight";
    private static final String COL_GOAL_USER_FK = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create TABLE 1: users
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create TABLE 2: daily_weights
        String createWeightsTable = "CREATE TABLE " + TABLE_WEIGHTS + " (" +
                COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_WEIGHT + " REAL NOT NULL, " +
                COL_USER_FK + " INTEGER, " +
                "FOREIGN KEY(" + COL_USER_FK + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createWeightsTable);

        // Create TABLE 3: goal_weight
        String createGoalTable = "CREATE TABLE " + TABLE_GOAL + " (" +
                COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GOAL_WEIGHT + " REAL NOT NULL, " +
                COL_GOAL_USER_FK + " INTEGER UNIQUE, " +
                "FOREIGN KEY(" + COL_GOAL_USER_FK + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createGoalTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);
        onCreate(db);
    }

    // ========== USER METHODS ==========

    // Validate user login
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Add new user
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Get user ID from username
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        }
        cursor.close();
        return -1;
    }

    // ========== WEIGHT METHODS ==========

    // Add daily weight
    public boolean addWeight(int userId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);
        values.put(COL_USER_FK, userId);

        long result = db.insert(TABLE_WEIGHTS, null, values);
        return result != -1;
    }

    // Get all weights for user (for grid)
    public List<WeightEntry> getAllWeights(int userId) {
        List<WeightEntry> weightList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WEIGHTS,
                new String[]{COL_WEIGHT_ID, COL_DATE, COL_WEIGHT},
                COL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String date = cursor.getString(1);
                double weight = cursor.getDouble(2);
                weightList.add(new WeightEntry(id, date, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return weightList;
    }

    // Delete weight entry
    public boolean deleteWeight(int weightId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_WEIGHTS,
                COL_WEIGHT_ID + "=?",
                new String[]{String.valueOf(weightId)});
        return result > 0;
    }

    // Get latest weight
    public double getLatestWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS,
                new String[]{COL_WEIGHT},
                COL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_DATE + " DESC", "1");

        if (cursor.moveToFirst()) {
            double weight = cursor.getDouble(0);
            cursor.close();
            return weight;
        }
        cursor.close();
        return 0.0;
    }

    // ========== GOAL METHODS ==========

    // Set or update goal weight
    public boolean setGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GOAL_WEIGHT, goalWeight);
        values.put(COL_GOAL_USER_FK, userId);

        // Check if goal already exists
        Cursor cursor = db.query(TABLE_GOAL,
                new String[]{COL_GOAL_ID},
                COL_GOAL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.getCount() > 0) {
            // Update existing goal
            cursor.moveToFirst();
            int goalId = cursor.getInt(0);
            cursor.close();
            int result = db.update(TABLE_GOAL, values,
                    COL_GOAL_ID + "=?",
                    new String[]{String.valueOf(goalId)});
            return result > 0;
        } else {
            // Insert new goal
            cursor.close();
            long result = db.insert(TABLE_GOAL, null, values);
            return result != -1;
        }
    }

    // Get goal weight
    public double getGoalWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOAL,
                new String[]{COL_GOAL_WEIGHT},
                COL_GOAL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            double goal = cursor.getDouble(0);
            cursor.close();
            return goal;
        }
        cursor.close();
        return 0.0; // No goal set
    }
}
