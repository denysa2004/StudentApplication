package com.example.studentapp;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationPickerActivity extends ComponentActivity {

    private MapView mapView;
    private EditText searchEditText;
    private Marker selectedLocationMarker;
    private GeoPoint selectedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.location_picker);

        mapView = findViewById(R.id.mapViewPicker);
        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        Button selectLocationButton = findViewById(R.id.selectLocationButton);

        setupMap();

        searchButton.setOnClickListener(v -> searchLocation());
        selectLocationButton.setOnClickListener(v -> selectLocation());
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(9.0);
        // Default to a central point (e.g., Paris)
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        selectedLocationMarker = new Marker(mapView);
        mapView.getOverlays().add(selectedLocationMarker);

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedPoint = p;
                updateMarker(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        mapView.getOverlays().add(new MapEventsOverlay(receiver));
    }

    private void searchLocation() {
        String locationName = searchEditText.getText().toString();
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                handler.post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                        selectedPoint = point;
                        IMapController mapController = mapView.getController();
                        mapController.animateTo(point, 15.0, 1000L);
                        updateMarker(point);
                    } else {
                        Toast.makeText(LocationPickerActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(LocationPickerActivity.this, "Geocoder failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateMarker(GeoPoint point) {
        selectedLocationMarker.setPosition(point);
        selectedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.invalidate(); // Redraw the map
    }

    private void selectLocation() {
        if (selectedPoint == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedPoint.getLatitude());
        resultIntent.putExtra("longitude", selectedPoint.getLongitude());

        // Also, reverse-geocode to get the address name
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String addressName = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(selectedPoint.getLatitude(), selectedPoint.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressName = address.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String finalAddressName = addressName;
            handler.post(() -> {
                resultIntent.putExtra("address", finalAddressName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
