package com.example.aifinancepredictor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class SecurityCodeActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private TextInputEditText etSecurityCode;
    private DatabaseHelper databaseHelper;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_code);

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (TextUtils.isEmpty(email)) {
            email = AuthSessionManager.getRememberedEmail(this);
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            openLoginAndFinish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        etSecurityCode = findViewById(R.id.etSecurityCode);
        Button btnVerifyCode = findViewById(R.id.btnVerifyCode);

        btnVerifyCode.setOnClickListener(v -> verifySecurityCode());
    }

    private void verifySecurityCode() {
        String securityCode = getText(etSecurityCode);

        if (!securityCode.matches("\\d{4}")) {
            Toast.makeText(this, "Please enter a valid 4-digit security code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkSecurityCode(email, securityCode)) {
            AuthSessionManager.setRememberedEmail(this, email);
            AuthSessionManager.setLoggedIn(this, true);
            AuthSessionManager.markSecurityVerifiedThisProcess();
            Toast.makeText(this, "Security code verified", Toast.LENGTH_SHORT).show();
            openDashboardAndFinish();
        } else {
            Toast.makeText(this, "Invalid security code", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDashboardAndFinish() {
        Intent intent = new Intent(SecurityCodeActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openLoginAndFinish() {
        Intent intent = new Intent(SecurityCodeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText inputEditText) {
        return inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
    }
}
