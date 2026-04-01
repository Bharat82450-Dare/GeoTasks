package com.example.geotask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GeoTaskDB_V2"; // Changed name to force fresh start
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_TASKS = "tasks";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESC = "description";
    private static final String KEY_ADDR = "address";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LNG = "longitude";
    private static final String KEY_DONE = "is_completed";

    // NEW COLUMNS
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_TYPE = "alert_type";
    private static final String KEY_START_TIME = "start_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESC + " TEXT,"
                + KEY_ADDR + " TEXT,"
                + KEY_LAT + " REAL,"
                + KEY_LNG + " REAL,"
                + KEY_DONE + " INTEGER DEFAULT 0,"
                + KEY_RADIUS + " INTEGER DEFAULT 500,"
                + KEY_TYPE + " TEXT,"
                + KEY_START_TIME + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // Add New Task (With new fields)
    public long addTask(String title, String desc, String addr, double lat, double lng, int radius, String type, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_DESC, desc);
        values.put(KEY_ADDR, addr);
        values.put(KEY_LAT, lat);
        values.put(KEY_LNG, lng);
        values.put(KEY_RADIUS, radius);
        values.put(KEY_TYPE, type);
        values.put(KEY_START_TIME, time);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    // Update Task (With new fields)
    public int updateTask(int id, String title, String desc, String addr, double lat, double lng, int radius, String type, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_DESC, desc);
        values.put(KEY_ADDR, addr);
        values.put(KEY_LAT, lat);
        values.put(KEY_LNG, lng);
        values.put(KEY_RADIUS, radius);
        values.put(KEY_TYPE, type);
        values.put(KEY_START_TIME, time);
        return db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateTaskStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DONE, status);
        db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<TaskModel> getAllTasks() {
        List<TaskModel> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " ORDER BY " + KEY_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                TaskModel task = new TaskModel(
                        cursor.getInt(0), // id
                        cursor.getString(1), // title
                        cursor.getString(2), // desc
                        cursor.getString(3), // address
                        cursor.getDouble(4), // lat
                        cursor.getDouble(5), // lng
                        cursor.getInt(6),     // isCompleted
                        cursor.getInt(7),     // radius
                        cursor.getString(8),  // alertType
                        cursor.getLong(9)     // startTime
                );
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }
}