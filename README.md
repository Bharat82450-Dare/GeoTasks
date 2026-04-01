# GeoTasks – Location Based Task Reminder Android App

GeoTasks is a location-based task reminder Android application that notifies users when they reach a specific location. The app uses GPS, Google Maps API, and a background geofence service to monitor the user’s location and trigger alerts when entering a defined radius.

## Features
- Create location-based tasks
- Select location using Google Maps
- Set trigger radius for each task
- Set alert type (Notification or Alarm)
- Set task activation time
- Background geofence service
- Mark task as completed from notification
- View all tasks on map
- Local database using SQLite

## Technologies Used
- Java (Android)
- Google Maps API
- Location Services (FusedLocationProvider)
- SQLite Database
- Foreground Service
- Notifications

## How It Works
The app continuously monitors the user’s location using a foreground service. When the user enters the radius of a saved task location, the app triggers a notification or alarm reminding the user to complete the task.

## Author
Bharat Sarkar
