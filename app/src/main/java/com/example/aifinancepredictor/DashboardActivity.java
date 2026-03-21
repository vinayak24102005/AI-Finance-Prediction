package com.example.aifinancepredictor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    // create dashboard activity
// fetch total income and expense from database
// calculate balance = income - expense
// display values
// add navigation buttons to other activities
    private DatabaseHelper databaseHelper;
    private TextView tvTodayTotalIncome;
    private TextView tvTodayTotalExpense;
    private TextView tvOverallBalance;
    private RecyclerView rvTodayTransactions;
    private TextView tvTodayTransactionsEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthSessionManager.isLoggedIn(this)) {
            openLoginAndFinish();
            return;
        }

        if (!AuthSessionManager.isSecurityVerifiedThisProcess()) {
            openSecurityAndFinish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        tvTodayTotalIncome = findViewById(R.id.tvTodayTotalIncome);
        tvTodayTotalExpense = findViewById(R.id.tvTodayTotalExpense);
        tvOverallBalance = findViewById(R.id.tvOverallBalance);
        rvTodayTransactions = findViewById(R.id.rvTodayTransactions);
        tvTodayTransactionsEmpty = findViewById(R.id.tvTodayTransactionsEmpty);
        rvTodayTransactions.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        Button btnPredictExpense = findViewById(R.id.btnPredictExpense);
        Button btnViewChart = findViewById(R.id.btnViewChart);

        btnAddExpense.setOnClickListener(v -> openScreen(AddExpenseActivity.class));
        btnViewHistory.setOnClickListener(v -> openScreen(HistoryActivity.class));
        btnPredictExpense.setOnClickListener(v -> openScreen(PredictionActivity.class));
        btnViewChart.setOnClickListener(v -> openScreen(ChartActivity.class));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        refreshDashboardTotals();
        loadTodayTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardTotals();
        loadTodayTransactions();
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(DashboardActivity.this, destination));
    }

    private void openLoginAndFinish() {
        Intent loginIntent = new Intent(DashboardActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void openSecurityAndFinish() {
        Intent securityIntent = new Intent(DashboardActivity.this, SecurityCodeActivity.class);
        securityIntent.putExtra(SecurityCodeActivity.EXTRA_EMAIL, AuthSessionManager.getRememberedEmail(this));
        securityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(securityIntent);
        finish();
    }

    private void refreshDashboardTotals() {
        if (databaseHelper == null) {
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        double totalIncome = databaseHelper.getTotalIncomeByDate(todayDate);
        double totalExpense = databaseHelper.getTotalExpenseByDate(todayDate);
        double overallBalance = databaseHelper.getTotalIncome() - databaseHelper.getTotalExpense();

        tvTodayTotalIncome.setText(String.format(Locale.getDefault(), "Today's Total Income: Rs %.2f", totalIncome));
        tvTodayTotalExpense.setText(String.format(Locale.getDefault(), "Today's Total Expense: Rs %.2f", totalExpense));
        tvOverallBalance.setText(String.format(Locale.getDefault(), "Overall Total Balance: Rs %.2f", overallBalance));
    }

    private void loadTodayTransactions() {
        if (databaseHelper == null) {
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = databaseHelper.getExpensesByDate(todayDate);
        List<ExpenseItem> items = new ArrayList<>();

        while (cursor.moveToNext()) {
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            items.add(new ExpenseItem(amount, category, date, type));
        }
        cursor.close();

        if (items.isEmpty()) {
            tvTodayTransactionsEmpty.setVisibility(View.VISIBLE);
            rvTodayTransactions.setVisibility(View.GONE);
            return;
        }

        tvTodayTransactionsEmpty.setVisibility(View.GONE);
        rvTodayTransactions.setVisibility(View.VISIBLE);
        rvTodayTransactions.setAdapter(new ExpenseAdapter(items));
    }
}
