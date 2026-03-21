package com.example.aifinancepredictor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    // fetch total income and expense
// display pie chart using MPAndroidChart
// show income and expense distribution
    private PieChart pieChart;
    private TextView tvChartSummary;
    private TextView tvNoChartData;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_view_chart);

        databaseHelper = new DatabaseHelper(this);
        pieChart = findViewById(R.id.pieChart);
        tvChartSummary = findViewById(R.id.tvChartSummary);
        tvNoChartData = findViewById(R.id.tvNoChartData);

        loadChartData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChartData();
    }

    private void loadChartData() {
        double totalIncome = databaseHelper.getTotalIncome();
        double totalExpense = databaseHelper.getTotalExpense();

        if (totalIncome <= 0 && totalExpense <= 0) {
            pieChart.setVisibility(View.GONE);
            tvChartSummary.setVisibility(View.GONE);
            tvNoChartData.setVisibility(View.VISIBLE);
            return;
        }

        tvNoChartData.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        tvChartSummary.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        if (totalIncome > 0) {
            entries.add(new PieEntry((float) totalIncome, "Income"));
        }
        if (totalExpense > 0) {
            entries.add(new PieEntry((float) totalExpense, "Expense"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Distribution");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(500);
        pieChart.invalidate();

        double balance = totalIncome - totalExpense;
        tvChartSummary.setText(String.format(
                Locale.getDefault(),
                "Income: Rs %.2f\nExpense: Rs %.2f\nBalance: Rs %.2f",
                totalIncome,
                totalExpense,
                balance
        ));
    }
}

