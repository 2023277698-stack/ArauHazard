package com.example.arauhazard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // 1. Get session data from SharedPreferences
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
            String role = pref.getString("role", "user");
            String name = pref.getString("userName", "User");

            if (isLoggedIn) {
                // 2. Route based on role to satisfy Server-Side vs Mobile requirements
                if ("admin".equals(role)) {
                    // ADMINS: Redirect to the Web Application (Task 2)
                    String webUrl = "http://10.0.2.2/streetsense/admin_view.php";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                    startActivity(browserIntent);
                } else {
                    // REGULAR USERS: Go to Mobile Home Screen (Task 1)
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    intent.putExtra("USER_NAME", name); // For the "Greet User" requirement
                    startActivity(intent);
                }
            } else {
                // 3. Not logged in: Go to Login screen
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            finish(); // Remove splash from backstack
        }, 2500);
    }
}