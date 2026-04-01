package com.example.geotask;

public class TaskModel {
    private int id;
    private String title;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private int isCompleted;

    // NEW FIELDS
    private int radius;        // e.g., 200 meters or 1000 meters
    private String alertType;  // "NOTIFICATION" or "ALARM"
    private long startTime;    // Timestamp (Don't remind before this time)

    // Updated Constructor
    public TaskModel(int id, String title, String description, String address, double latitude, double longitude, int isCompleted, int radius, String alertType, long startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCompleted = isCompleted;
        this.radius = radius;
        this.alertType = alertType;
        this.startTime = startTime;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getIsCompleted() { return isCompleted; }
    public int getRadius() { return radius; }
    public String getAlertType() { return alertType; }
    public long getStartTime() { return startTime; }
}