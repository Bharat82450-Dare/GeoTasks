package com.example.geotask;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;

public class AllTasksMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private DatabaseHelper dbHelper;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks_map);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_all_tasks);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable My Location Layer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location userLocation) {
                    showTasksAndUser(userLocation);
                }
            });
        } else {
            showTasksAndUser(null);
        }
    }

    private void showTasksAndUser(Location userLocation) {
        List<TaskModel> taskList = dbHelper.getAllTasks();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int validPinsCount = 0;
        int zeroCount = 0;

        for (TaskModel task : taskList) {
            double lat = task.getLatitude();
            double lng = task.getLongitude();

            // DEBUG: CHECK FOR 0.0
            if (lat == 0.0 && lng == 0.0) {
                zeroCount++; // Count how many bad tasks we have
                continue; // Skip adding the pin
            }

            // If we get here, the location is VALID
            LatLng loc = new LatLng(lat, lng);

            // Color Logic: Red for Pending, Green for Done
            float markerColor = (task.getIsCompleted() == 1) ?
                    BitmapDescriptorFactory.HUE_GREEN :
                    BitmapDescriptorFactory.HUE_RED;

            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title(task.getTitle())
                    .snippet(task.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

            builder.include(loc);
            validPinsCount++;
        }

        // DEBUG MESSAGE
        if (zeroCount > 0) {
            Toast.makeText(this, "Warning: " + zeroCount + " tasks have 0.0 location (Hidden)", Toast.LENGTH_LONG).show();
        }

        // Add User Location to view
        if (userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            builder.include(userLatLng);
            validPinsCount++;
        }

        // Adjust Camera
        if (validPinsCount > 0) {
            try {
                // If we have points, zoom to show them
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
            } catch (Exception e) {
                // Fallback: Just zoom to user
                if (userLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 15));
                }
            }
        }
    }
}