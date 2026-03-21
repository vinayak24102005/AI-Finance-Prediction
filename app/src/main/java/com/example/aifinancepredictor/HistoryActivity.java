package com.example.aifinancepredictor;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);
        rvExpenses = findViewById(R.id.rvExpenses);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);
        Button btnResetFilter = findViewById(R.id.btnResetFilter);

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        bindDatePickers();

        btnApplyFilter.setOnClickListener(v -> applyFilter());
        btnResetFilter.setOnClickListener(v -> {
            etFromDate.setText("");
            etToDate.setText("");
            loadExpenses(null, null);
        });

        loadExpenses(null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(getTrimmed(etFromDate), getTrimmed(etToDate));
    }

    private void loadExpenses(String fromDate, String toDate) {
        List<ExpenseItem> items = new ArrayList<>();
        Cursor cursor;
        if (!TextUtils.isEmpty(fromDate) && !TextUtils.isEmpty(toDate)) {
            cursor = databaseHelper.getExpensesByDateRange(fromDate, toDate);
        } else {
            cursor = databaseHelper.getAllExpenses();
        }

        while (cursor.moveToNext()) {
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            items.add(new ExpenseItem(amount, category, date, type));
        }
        cursor.close();

        if (items.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvExpenses.setVisibility(View.GONE);
            return;
        }

        tvEmptyHistory.setVisibility(View.GONE);
        rvExpenses.setVisibility(View.VISIBLE);
        rvExpenses.setAdapter(new ExpenseAdapter(items));
    }

    private void bindDatePickers() {
        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate));
    }

    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> target.setText(
                        String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                ),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void applyFilter() {
        String fromDate = getTrimmed(etFromDate);
        String toDate = getTrimmed(etToDate);

        if (TextUtils.isEmpty(fromDate) && TextUtils.isEmpty(toDate)) {
            loadExpenses(null, null);
            return;
        }

        if (TextUtils.isEmpty(fromDate) || TextUtils.isEmpty(toDate)) {
            Toast.makeText(this, "Please select both From and To dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromDate.compareTo(toDate) > 0) {
            Toast.makeText(this, "From date cannot be after To date", Toast.LENGTH_SHORT).show();
            return;
        }

        loadExpenses(fromDate, toDate);
    }

    private String getTrimmed(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

