package com.example.arauhazard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Added

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Get user session info
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = getIntent().getStringExtra("USER_NAME");

        if (name == null) {
            name = pref.getString("userName", "User");
        }

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + name + "!");

        // 2. Initialize UI Components (Matching your XML IDs)
        CardView cardReport = findViewById(R.id.cardReport); // Match XML: android:id="@+id/cardReport"
        CardView cardMap = findViewById(R.id.cardMap);       // Match XML: android:id="@+id/cardMap"
        Button btnAbout = findViewById(R.id.btnAbout);      // Match XML: android:id="@+id/btnAbout"
        Button btnLogout = findViewById(R.id.btnLogout);    // Match XML: android:id="@+id/btnLogout"

        // 3. Set Click Listeners
        if (cardReport != null) {
            String finalName = name;
            cardReport.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
                intent.putExtra("USER_NAME", finalName);
                startActivity(intent);
            });
        }

        if (cardMap != null) {
            cardMap.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, MapActivity.class));
            });
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                // Change AboutActivity.class to your actual About Activity class name
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }
    }

    private void logoutUser() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}