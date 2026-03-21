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

public class RegisterActivity extends AppCompatActivity {

    // create register activity
// validate inputs
// store user in sqlite
// show success message
// redirect to login
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputEditText etSecurityCode;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etSecurityCode = findViewById(R.id.etSecurityCode);
        databaseHelper = new DatabaseHelper(this);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin(false));
    }

    private void handleRegister() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String securityCode = getText(etSecurityCode);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(securityCode)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!securityCode.matches("\\d{4}")) {
            Toast.makeText(this, "Security code must be exactly 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (insertUser(name, email, password, securityCode)) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            navigateToLogin(true);
        }
    }

    private boolean insertUser(String name, String email, String password, String securityCode) {
        try {
            boolean isInserted = databaseHelper.insertUser(name, email, password, securityCode);
            if (!isInserted) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            }
            return isInserted;
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "Registration failed. Try again", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void navigateToLogin(boolean clearAuthStack) {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        if (clearAuthStack) {
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(loginIntent);
        finish();
    }

    private String getText(TextInputEditText inputEditText) {
        return inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
    }
}
