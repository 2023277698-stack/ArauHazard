package com.example.arauhazard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    // Declare variables to match your XML IDs
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Ensure this matches your XML filename

        // 1. Initialize UI Components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // 2. Set Click Listener
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // Capture input
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 3. Simple Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Send data to XAMPP Server
        sendToDatabase(name, email, password);
    }

    private void sendToDatabase(String name, String email, String password) {
        String url = "http://10.0.2.2/arauhazard/signup.php"; // Localhost for emulator

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    try {
                        // Check if the PHP script returned success
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(this, "Success! Please Login.", Toast.LENGTH_LONG).show();
                            finish(); // Go back to Login screen
                        } else {
                            Toast.makeText(this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Connection failed: Check XAMPP", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}