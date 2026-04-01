package com.example.geotask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TASK_ID", -1);

        if (taskId != -1) {
            // 1. Mark the task as DONE in Database
            DatabaseHelper db = new DatabaseHelper(context);
            db.updateTaskStatus(taskId, 1); // 1 = Done

            // 2. Cancel the Notification (Stops the Alarm Sound)
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(taskId);

            Toast.makeText(context, "Alarm Stopped & Task Completed", Toast.LENGTH_SHORT).show();
        }
    }
}