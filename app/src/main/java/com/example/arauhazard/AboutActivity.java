package com.example.arauhazard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 1. Initialize the Back Button
        Button btnBack = findViewById(R.id.btnBackAbout);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // Returns to HomeActivity
        }

        // 2. Optional: Manual Listener for GitHub if autoLink is not enough
        // Replace with your actual repository URL
        TextView tvGithub = findViewById(android.R.id.text1); // Example selector
        // Since you used android:autoLink="web" in XML, it should work automatically.
        // But if you want a custom click handler for marks:
        /*
        tvGithub.setOnClickListener(v -> {
            String url = "https://github.com/2023277698-stack/ArauHazard;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        */
    }
}