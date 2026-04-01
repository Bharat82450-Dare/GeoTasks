package com.example.geotask;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnConfirm;
    // FIX 1: Initialize with a dummy default so it never crashes
    private LatLng selectedLocation = new LatLng(0.0, 0.0);
    private String selectedAddress = "";
    private FusedLocationProviderClient fusedLocationClient;

    // TODO: CHECK YOUR API KEY IS HERE
    private static final String API_KEY = "AIzaSyCdTRCLXmxuQhleKrdd4boTNlMUC6xDPPg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        btnConfirm = findViewById(R.id.btnConfirmLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupAutocomplete();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIX 2: Prevent sending if location is still 0.0
                if (selectedLocation.latitude == 0.0 && selectedLocation.longitude == 0.0) {
                    Toast.makeText(MapPickerActivity.this, "Wait! Location not selected yet.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // FIX 3: Debug Toast to prove we have data
                Toast.makeText(MapPickerActivity.this,
                        "Sending: " + selectedLocation.latitude, Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                resultIntent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void setupAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        selectedLocation = latLng; // Update variable
                        selectedAddress = place.getName();
                        btnConfirm.setText("Select: " + selectedAddress);
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                    }
                }
                @Override
                public void onError(@NonNull Status status) { }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true); // Permission check needed here usually

        // CHECK IF WE RECEIVED A LOCATION TO EDIT
        double prevLat = getIntent().getDoubleExtra("PREVIOUS_LAT", 0.0);
        double prevLng = getIntent().getDoubleExtra("PREVIOUS_LNG", 0.0);

        if (prevLat != 0.0 && prevLng != 0.0) {
            // Case A: Editing -> Go to Saved Location
            LatLng savedLoc = new LatLng(prevLat, prevLng);
            selectedLocation = savedLoc;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(savedLoc, 16));
            getAddressFromLocation(prevLat, prevLng); // Show address on button
        } else {
            // Case B: New Task -> Go to Current Location (Blue Dot)
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                }
            });
        }

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                selectedLocation = mMap.getCameraPosition().target;
                getAddressFromLocation(selectedLocation.latitude, selectedLocation.longitude);
            }
        });
    }

    private void getAddressFromLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedAddress = addresses.get(0).getAddressLine(0);
                btnConfirm.setText("Select: " + addresses.get(0).getFeatureName());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}