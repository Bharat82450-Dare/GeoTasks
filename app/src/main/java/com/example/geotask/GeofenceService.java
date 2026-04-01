package com.example.geotask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.util.List;

public class GeofenceService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;

    // Final Channel IDs
    private static final String CHANNEL_NORMAL = "geo_channel_normal_final";
    private static final String CHANNEL_ALARM = "geo_channel_alarm_final";

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannels();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    checkDistances(location);
                }
            }
        };

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        // CHANGED: setMinUpdateDistanceMeters is set to 0
        // This ensures it checks every 5-10 seconds EVEN IF YOU ARE SITTING STILL.
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5 seconds
                .setMinUpdateDistanceMeters(0) // Check even if I haven't moved
                .setMaxUpdateDelayMillis(10000) // Force delivery
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("GeoTask", "Permission missing for location");
        }
    }

    private void checkDistances(Location currentLocation) {
        List<TaskModel> tasks = dbHelper.getAllTasks();

        for (TaskModel task : tasks) {
            // 1. Check if Pending
            if (task.getIsCompleted() == 0) {

                // 2. Check Time Constraint
                if (System.currentTimeMillis() < task.getStartTime()) {
                    continue;
                }

                // 3. Check Distance
                float[] results = new float[1];
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        task.getLatitude(), task.getLongitude(), results);

                if (results[0] < task.getRadius()) {
                    sendNotification(task);
                }
            }
        }
    }

    private void sendNotification(TaskModel task) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = CHANNEL_NORMAL;
        boolean isAlarm = false;

        if (task.getAlertType() != null && task.getAlertType().equalsIgnoreCase("ALARM")) {
            channelId = CHANNEL_ALARM;
            isAlarm = true;
        }

        // Setup "Mark as Done" Action Button
        Intent actionIntent = new Intent(this, NotificationActionReceiver.class);
        actionIntent.putExtra("TASK_ID", task.getId());

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(
                this, task.getId(), actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Arrived: " + task.getTitle())
                .setContentText("You have reached the location!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Mark Done", actionPendingIntent);

        if (isAlarm) {
            builder.setOngoing(true);
            builder.setVibrate(new long[]{0, 500, 200, 500, 200, 1000});
        }

        Notification notification = builder.build();

        if (isAlarm) {
            notification.flags |= Notification.FLAG_INSISTENT;
        }

        manager.notify(task.getId(), notification);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // Normal Channel
            NotificationChannel normalChannel = new NotificationChannel(
                    CHANNEL_NORMAL, "GeoTask Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(normalChannel);

            // Alarm Channel
            NotificationChannel alarmChannel = new NotificationChannel(
                    CHANNEL_ALARM, "GeoTask Alarms", NotificationManager.IMPORTANCE_HIGH);

            Uri alarmSound = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            alarmChannel.setSound(alarmSound, audioAttributes);
            alarmChannel.enableVibration(true);
            manager.createNotificationChannel(alarmChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_NORMAL)
                .setContentTitle("GeoTask is Active")
                .setContentText("Monitoring location...")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}