package com.example.aifinancepredictor;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    // fetch all transactions from database
// display in recycler view
// show amount, category, date and type
// handle empty list case
    private RecyclerView rvExpenses;
    private TextView tvEmptyHistory;
    private EditText etFromDate;
    private EditText etToDate;
    private DatabaseHelper databaseHelper;
    private String selectedFromDate;
    private String selectedToDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);
        rvExpenses = findViewById(R.id.rvExpenses);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        bindPickers();

        selectedFromDate = "";
        selectedToDate = "";

        loadAllExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyActiveFilter();
    }

    private void loadAllExpenses() {
        Cursor cursor = databaseHelper.getAllExpenses();
        renderList(cursor, getString(R.string.history_empty_default));
    }

    private void loadExpensesByDateRange(String fromDate, String toDate) {
        Cursor cursor = databaseHelper.getExpensesByDateRange(fromDate, toDate);
        renderList(cursor, getString(R.string.history_empty_for_range, fromDate, toDate));
    }

    private void renderList(Cursor cursor, String emptyMessage) {
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
            tvEmptyHistory.setText(emptyMessage);
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvExpenses.setVisibility(View.GONE);
            return;
        }

        tvEmptyHistory.setVisibility(View.GONE);
        rvExpenses.setVisibility(View.VISIBLE);
        rvExpenses.setAdapter(new ExpenseAdapter(items));
    }

    private void bindPickers() {
        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFromDate) {
        final Calendar calendar = Calendar.getInstance();
        String currentValue = isFromDate ? selectedFromDate : selectedToDate;
        if (!TextUtils.isEmpty(currentValue) && currentValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
            int year = Integer.parseInt(currentValue.substring(0, 4));
            int month = Integer.parseInt(currentValue.substring(5, 7)) - 1;
            int day = Integer.parseInt(currentValue.substring(8, 10));
            calendar.set(year, month, day);
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    if (isFromDate) {
                        selectedFromDate = selectedDate;
                        etFromDate.setText(selectedDate);
                    } else {
                        selectedToDate = selectedDate;
                        etToDate.setText(selectedDate);
                    }
                    applyDateFilterIfReady();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void applyDateFilterIfReady() {
        if (TextUtils.isEmpty(selectedFromDate) || TextUtils.isEmpty(selectedToDate)) {
            return;
        }
        if (selectedFromDate.compareTo(selectedToDate) > 0) {
            Toast.makeText(this, R.string.history_invalid_date_range, Toast.LENGTH_SHORT).show();
            return;
        }
        loadExpensesByDateRange(selectedFromDate, selectedToDate);
    }

    private void applyActiveFilter() {
        if (!TextUtils.isEmpty(selectedFromDate)
                && !TextUtils.isEmpty(selectedToDate)
                && selectedFromDate.compareTo(selectedToDate) <= 0) {
            loadExpensesByDateRange(selectedFromDate, selectedToDate);
            return;
        }
        loadAllExpenses();
    }

}

