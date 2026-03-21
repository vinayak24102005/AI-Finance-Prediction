package com.example.aifinancepredictor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    // create add transaction activity
// get inputs: amount, category, date, type
// validate inputs
// save transaction in sqlite database
// show toast after saving
// clear fields after save
    private static final String[] EXPENSE_CATEGORIES = {"Food", "Transport", "Shopping", "Bills", "Other"};
    private static final String[] INCOME_CATEGORIES = {"Salary", "Other"};

    private EditText etAmount;
    private EditText etDate;
    private Spinner spinnerCategory;
    private Spinner spinnerType;
    private DatabaseHelper databaseHelper;
    private final Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_add_expense);

        databaseHelper = new DatabaseHelper(this);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerType = findViewById(R.id.spinnerType);
        Button btnSaveTransaction = findViewById(R.id.btnSaveExpense);

        setupTypeSpinner();
        updateCategorySpinnerForType(normalizeType(spinnerType.getSelectedItem() == null ? "Expense" : spinnerType.getSelectedItem().toString()));
        bindTypeCategoryBehavior();
        bindDatePicker();
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void updateCategorySpinnerForType(String type) {
        String[] categories = "income".equals(type) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void bindTypeCategoryBehavior() {
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                updateCategorySpinnerForType(normalizeType(selectedType));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateCategorySpinnerForType("expense");
            }
        });
    }

    private void setupTypeSpinner() {
        String[] types = {"Expense", "Income"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void bindDatePicker() {
        updateDateField();
        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateField() {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        etDate.setText(formattedDate);
    }

    private void saveTransaction() {
        String amountText = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
        String date = etDate.getText() == null ? "" : etDate.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() == null ? "Other" : spinnerCategory.getSelectedItem().toString();
        String selectedType = spinnerType.getSelectedItem() == null ? "Expense" : spinnerType.getSelectedItem().toString();
        String type = normalizeType(selectedType);

        if (TextUtils.isEmpty(amountText) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please enter amount and date", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.insertExpense(amount, category, date, type)) {
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            spinnerCategory.setSelection(0);
            spinnerType.setSelection(0);
            selectedDate.setTimeInMillis(System.currentTimeMillis());
            updateDateField();
        } else {
            Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private String normalizeType(String selectedType) {
        return "income".equalsIgnoreCase(selectedType) ? "income" : "expense";
    }
}
