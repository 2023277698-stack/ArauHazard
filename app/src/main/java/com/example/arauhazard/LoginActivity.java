package com.example.arauhazard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignup = findViewById(R.id.tvGoToSignup);

        btnLogin.setOnClickListener(v -> loginUser());

        tvGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/arauhazard/login.php";
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("username", username);
            loginData.put("password", password);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, loginData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            String role = response.getString("role");
                            String name = response.getString("name");

                            // CHECK ROLE: If Admin, deny app access and direct to Web
                            if ("admin".equalsIgnoreCase(role)) {
                                Toast.makeText(this, "Admins must use the Web Portal", Toast.LENGTH_LONG).show();

                                // Optional: Automatically open the browser for them
                                String webUrl = "http://10.0.2.2/arauhazard/admin_view.php";
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                                startActivity(browserIntent);
                            } else {
                                // Normal User - Save session and go to HomeActivity
                                SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                pref.edit().putString("userName", name).apply();

                                Intent intent = new Intent(this, HomeActivity.class);
                                intent.putExtra("USER_NAME", name);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}