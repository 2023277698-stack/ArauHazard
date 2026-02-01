package com.example.arauhazard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final LatLng ARAU_CENTER = new LatLng(6.4326, 100.2762);

    // Cache to prevent infinite reloading loops
    private final Map<String, Boolean> imagesLoadedMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ARAU_CENTER, 14.0f));

        loadMarkersFromServer();
        enableUserLocation();
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        public CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.layout_info_window, null);
        }

        private void renderWindowText(Marker marker, View view) {
            TextView title = view.findViewById(R.id.txtHazardTitle);
            TextView snippet = view.findViewById(R.id.txtReporterName);
            ImageView imgHazard = view.findViewById(R.id.imgHazard);

            title.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());

            String imagePath = (String) marker.getTag();

            if (imagePath != null && !imagePath.isEmpty()) {
                imgHazard.setVisibility(View.VISIBLE);
                String imageUrl = "http://10.0.2.2/streetsense/uploads/" + imagePath;

                // Check if we already loaded this image to avoid infinite refresh
                boolean isLoaded = imagesLoadedMap.containsKey(imagePath) && imagesLoadedMap.get(imagePath);

                if (isLoaded) {
                    Glide.with(MapActivity.this)
                            .load(imageUrl)
                            .override(200, 120)
                            .centerCrop()
                            .into(imgHazard);
                } else {
                    Glide.with(MapActivity.this)
                            .load(imageUrl)
                            .override(200, 120)
                            .centerCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imagesLoadedMap.put(imagePath, true);
                                    // Refresh the info window safely using the main looper
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        if (marker.isInfoWindowShown()) {
                                            marker.showInfoWindow();
                                        }
                                    }, 100);
                                    return false;
                                }
                            })
                            .into(imgHazard);
                }
            } else {
                imgHazard.setVisibility(View.GONE);
            }
        }

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            renderWindowText(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null; // Using getInfoWindow so this is ignored
        }
    }

    private void loadMarkersFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/streetsense/get_report.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);

                JSONArray array = new JSONArray(json.toString());

                runOnUiThread(() -> {
                    try {
                        mMap.clear();
                        imagesLoadedMap.clear(); // Clear cache on reload
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            LatLng pos = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));

                            String type = obj.optString("hazard_type", "Pothole");
                            String imgFile = obj.optString("image_path", "");
                            String reporter = obj.optString("name", "Anonymous");

                            float markerColor = getMarkerColor(type);

                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(type)
                                    .snippet("By: " + reporter)
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                            if (m != null) m.setTag(imgFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private float getMarkerColor(String type) {
        switch (type) {
            case "Flash Flood": return BitmapDescriptorFactory.HUE_AZURE;
            case "Road Accident": return BitmapDescriptorFactory.HUE_RED;
            case "Broken Streetlight": return BitmapDescriptorFactory.HUE_YELLOW;
            case "Fallen Tree": return BitmapDescriptorFactory.HUE_ORANGE;
            default: return BitmapDescriptorFactory.HUE_GREEN;
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }
}