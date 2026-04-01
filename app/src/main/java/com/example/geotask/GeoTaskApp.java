package com.example.geotask;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class GeoTaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // This command turns on the magic "Wallpaper Colors"
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}