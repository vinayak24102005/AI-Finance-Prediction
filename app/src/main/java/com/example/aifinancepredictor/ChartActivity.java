package com.example.aifinancepredictor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    // fetch total income and expense
// display pie chart using MPAndroidChart
// show income and expense distribution
    private PieChart pieChart;
    private EditText etChartMonth;
    private TextView tvChartSummary;
    private TextView tvNoChartData;
    private DatabaseHelper databaseHelper;
    private String selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_view_chart);

        databaseHelper = new DatabaseHelper(this);
        pieChart = findViewById(R.id.pieChart);
        etChartMonth = findViewById(R.id.etChartMonth);
        tvChartSummary = findViewById(R.id.tvChartSummary);
        tvNoChartData = findViewById(R.id.tvNoChartData);

        selectedMonth = getCurrentYearMonth();
        etChartMonth.setText(selectedMonth);
        etChartMonth.setOnClickListener(v -> showMonthPicker());

        loadChartData(selectedMonth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChartData(selectedMonth);
    }

    private void loadChartData(String yearMonth) {
        double totalIncome = databaseHelper.getTotalIncomeByMonth(yearMonth);
        double totalExpense = databaseHelper.getTotalExpenseByMonth(yearMonth);
        double total = totalIncome + totalExpense;

        if (total <= 0) {
            pieChart.setVisibility(View.GONE);
            tvChartSummary.setVisibility(View.GONE);
            tvNoChartData.setVisibility(View.VISIBLE);
            tvNoChartData.setText(getString(R.string.chart_no_data_for_month, yearMonth));
            return;
        }

        tvNoChartData.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        tvChartSummary.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        if (totalIncome > 0) {
            entries.add(new PieEntry((float) totalIncome, getString(R.string.chart_income_label)));
        }
        if (totalExpense > 0) {
            entries.add(new PieEntry((float) totalExpense, getString(R.string.chart_expense_label)));
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.chart_distribution_label));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(500);
        pieChart.invalidate();

        double balance = totalIncome - totalExpense;
        double incomePercent = (totalIncome / total) * 100d;
        double expensePercent = (totalExpense / total) * 100d;
        tvChartSummary.setText(String.format(
                Locale.getDefault(),
                getString(R.string.chart_summary_month_format),
                yearMonth,
                totalIncome,
                incomePercent,
                totalExpense,
                expensePercent,
                balance
        ));
    }

    private void showMonthPicker() {
        final Calendar calendar = Calendar.getInstance();
        if (selectedMonth != null && selectedMonth.matches("\\d{4}-\\d{2}")) {
            int year = Integer.parseInt(selectedMonth.substring(0, 4));
            int month = Integer.parseInt(selectedMonth.substring(5, 7)) - 1;
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = String.format(Locale.getDefault(), "%04d-%02d", year, month + 1);
                    etChartMonth.setText(selectedMonth);
                    loadChartData(selectedMonth);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private String getCurrentYearMonth() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%04d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }
}

