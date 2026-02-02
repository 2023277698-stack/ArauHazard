package com.example.arauhazard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERM_CODE = 101;
    private static final int LOCATION_PERM_CODE = 102;

    private ImageView imgPreview;
    private Button btnCamera, btnSubmit;
    private ImageButton btnBack;
    private Spinner spinnerHazard;
    private ProgressBar uploadProgress;
    private String username;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean imageCaptured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // 1. Get User Session Info
        username = getIntent().getStringExtra("USER_NAME");
        if (username == null) username = "User";

        // 2. Initialize UI Components
        imgPreview = findViewById(R.id.imgPreview);
        btnCamera = findViewById(R.id.btnCamera);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack); // Matched with XML ID
        spinnerHazard = findViewById(R.id.spinnerHazard);
        uploadProgress = findViewById(R.id.uploadProgress);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 3. Back Button Functionality
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 4. Camera Button
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
            } else {
                openCamera();
            }
        });

        // 5. Submit Button
        btnSubmit.setOnClickListener(v -> {
            if (!imageCaptured) {
                Toast.makeText(this, "Please take a photo first!", Toast.LENGTH_SHORT).show();
                return;
            }
            checkLocationAndSend();
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void checkLocationAndSend() {
        uploadProgress.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            uploadProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERM_CODE);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        // Geofencing: Check if user is in Arau (approx 20km radius)
                        float[] results = new float[1];
                        Location.distanceBetween(lat, lng, 6.4326, 100.2762, results);

                        if (results[0] > 20000) {
                            uploadProgress.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(this, "Error: Must be in Arau to report!", Toast.LENGTH_LONG).show();
                        } else {
                            String type = spinnerHazard.getSelectedItem().toString();
                            sendDataToServer(username, lat, lng, type);
                        }
                    } else {
                        uploadProgress.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, "Location error. Enable GPS.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendDataToServer(String name, double lat, double lng, String type) {
        // NOTE: Use your PC's IP if testing on real device, or 10.0.2.2 for emulator
        String url = "http://10.0.2.2/arauhazard/upload_image.php";
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        String userAgent = System.getProperty("http.agent");
        Bitmap bitmap = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        String encodedImage = encodeImage(bitmap);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("latitude", lat);
            jsonBody.put("longitude", lng);
            jsonBody.put("hazard_type", type);
            jsonBody.put("user_agent", userAgent);
            jsonBody.put("image_data", encodedImage);
            jsonBody.put("time", time);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    uploadProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Report Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    uploadProgress.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Log.e("VOLLEY_ERR", error.toString());
                    Toast.makeText(this, "Server error. Try again.", Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgPreview.setImageBitmap(photo);
            imageCaptured = true;
        }
    }
}