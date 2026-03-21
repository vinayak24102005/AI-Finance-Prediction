package com.example.aifinancepredictor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthSessionManager.isLoggedIn(this)) {
            openSecurityVerificationAndFinish(AuthSessionManager.getRememberedEmail(this));
            return;
        }

        super.setContentView(R.layout.activity_login);
        databaseHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkUserCredentials(email, password)) {
            AuthSessionManager.setRememberedEmail(this, email);
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            openSecurityVerificationAndFinish(email);
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkUserCredentials(String email, String password) {
        try {
            return databaseHelper.checkUser(email, password);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to verify user. Please sign up first.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void openSecurityVerificationAndFinish(String email) {
        Intent intent = new Intent(LoginActivity.this, SecurityCodeActivity.class);
        intent.putExtra(SecurityCodeActivity.EXTRA_EMAIL, email);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText inputEditText) {
        return inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
    }
}
