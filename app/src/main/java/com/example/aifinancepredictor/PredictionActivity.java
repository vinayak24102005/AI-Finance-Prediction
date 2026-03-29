package com.example.aifinancepredictor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PredictionActivity extends AppCompatActivity {

    private static final String PREDICTION_URL = "https://finance-api-254i.onrender.com/predict";
    private static final int CATEGORY_FOOD = 0;
    private static final int CATEGORY_TRANSPORT = 1;
    private static final int CATEGORY_SHOPPING = 2;

    private TextInputEditText etFood;
    private TextInputEditText etTransport;
    private TextInputEditText etShopping;
    private TextInputEditText etBudget;
    private Button btnPredict;
    private ProgressBar progressPrediction;
    private TextView tvPredictionStatus;
    private TextView tvPredictionResult;
    private TextView tvExpectedSavings;
    private TextView tvSmartSuggestion;
    private TextView tvAutofillInfo;

    private RequestQueue requestQueue;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_prediction);

        etFood = findViewById(R.id.etFood);
        etTransport = findViewById(R.id.etTransport);
        etShopping = findViewById(R.id.etShopping);
        etBudget = findViewById(R.id.etBudget);
        btnPredict = findViewById(R.id.btnPredict);
        progressPrediction = findViewById(R.id.progressPrediction);
        tvPredictionStatus = findViewById(R.id.tvPredictionStatus);
        tvPredictionResult = findViewById(R.id.tvPredictionResult);
        tvExpectedSavings = findViewById(R.id.tvExpectedSavings);
        tvSmartSuggestion = findViewById(R.id.tvSmartSuggestion);
        tvAutofillInfo = findViewById(R.id.tvAutofillInfo);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        databaseHelper = new DatabaseHelper(this);

        prefillCurrentMonthExpenseFields();

        btnPredict.setOnClickListener(v -> fetchPrediction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefillCurrentMonthExpenseFields();
    }

    private void fetchPrediction() {
        String foodText = getText(etFood);
        String transportText = getText(etTransport);
        String shoppingText = getText(etShopping);
        String budgetText = getText(etBudget);

        if (TextUtils.isEmpty(foodText) || TextUtils.isEmpty(transportText)
                || TextUtils.isEmpty(shoppingText) || TextUtils.isEmpty(budgetText)) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double food;
        double transport;
        double shopping;
        double budget;
        try {
            food = Double.parseDouble(foodText);
            transport = Double.parseDouble(transportText);
            shopping = Double.parseDouble(shoppingText);
            budget = Double.parseDouble(budgetText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_invalid_numbers, Toast.LENGTH_SHORT).show();
            return;
        }

        if (food < 0 || transport < 0 || shopping < 0 || budget < 0) {
            Toast.makeText(this, R.string.error_negative_values, Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("food", food);
            requestBody.put("transport", transport);
            requestBody.put("shopping", shopping);
        } catch (JSONException e) {
            Toast.makeText(this, R.string.error_parse_request, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true, getString(R.string.prediction_validating));
        clearResults();
        runPredictionRequest(requestBody, food, transport, shopping, budget);
    }

    private void runPredictionRequest(JSONObject requestBody, double food, double transport,
                                      double shopping, double budget) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                PREDICTION_URL,
                requestBody,
                response -> {
                    setLoading(false, "");
                    double predictedExpense = getDoubleFromResponse(response, "predicted_expense");
                    if (Double.isNaN(predictedExpense)) {
                        predictedExpense = getDoubleFromResponse(response, "prediction");
                    }

                    if (Double.isNaN(predictedExpense)) {
                        tvPredictionResult.setText(R.string.error_missing_prediction_key);
                        tvPredictionStatus.setText(R.string.status_failed);
                        tvPredictionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_high));
                        return;
                    }

                    renderPrediction(predictedExpense, food, transport, shopping, budget);
                },
                error -> {
                    setLoading(false, "");
                    tvPredictionStatus.setText(R.string.status_failed);
                    tvPredictionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_high));

                    String errorMessage = getString(R.string.error_prediction_failed);
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String serverResponse = new String(networkResponse.data, StandardCharsets.UTF_8);
                        if (!TextUtils.isEmpty(serverResponse)) {
                            errorMessage = getString(R.string.error_api_error) + " " + serverResponse;
                        }
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void setLoading(boolean loading, String message) {
        btnPredict.setEnabled(!loading);
        progressPrediction.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvPredictionStatus.setText(message);
        tvPredictionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
    }

    private void renderPrediction(double predictedExpense, double food, double transport,
                                  double shopping, double budget) {
        double savings = budget - predictedExpense;

        String expenseLevel;
        int statusColorRes;
        if (predictedExpense < 2000) {
            expenseLevel = getString(R.string.status_low);
            statusColorRes = R.color.status_low;
        } else if (predictedExpense <= 4000) {
            expenseLevel = getString(R.string.status_moderate);
            statusColorRes = R.color.status_moderate;
        } else {
            expenseLevel = getString(R.string.status_high);
            statusColorRes = R.color.status_high;
        }

        tvPredictionResult.setText(getString(R.string.amount_currency_format, predictedExpense));

        String savingsText;
        int savingsColor;
        if (savings >= 0) {
            savingsText = getString(R.string.savings_remaining_format, savings);
            savingsColor = R.color.status_low;
        } else {
            savingsText = getString(R.string.savings_over_format, Math.abs(savings));
            savingsColor = R.color.status_high;
        }
        tvExpectedSavings.setText(savingsText);
        tvExpectedSavings.setTextColor(ContextCompat.getColor(this, savingsColor));

        tvPredictionStatus.setText(expenseLevel);
        tvPredictionStatus.setTextColor(ContextCompat.getColor(this, statusColorRes));
        tvSmartSuggestion.setText(buildSuggestion(food, transport, shopping, savings, budget, predictedExpense));
    }

    private String buildSuggestion(double food, double transport, double shopping,
                                   double savings, double budget, double predictedExpense) {
        int highestCategory = getHighestCategory(food, transport, shopping);

        if (savings < 0) {
            return getString(
                    R.string.suggestion_overspending,
                    Math.abs(savings),
                    getCategoryLabel(highestCategory)
            );
        }

        switch (highestCategory) {
            case CATEGORY_FOOD:
                return getString(R.string.suggestion_food);
            case CATEGORY_TRANSPORT:
                return getString(R.string.suggestion_transport);
            case CATEGORY_SHOPPING:
                return getString(R.string.suggestion_shopping);
            default:
                break;
        }

        if (budget > 0 && predictedExpense >= (budget * 0.8)) {
            return getString(R.string.suggestion_close_budget);
        }

        return getString(R.string.suggestion_on_track);
    }

    private int getHighestCategory(double food, double transport, double shopping) {
        if (food >= transport && food >= shopping) {
            return CATEGORY_FOOD;
        }
        if (transport >= food && transport >= shopping) {
            return CATEGORY_TRANSPORT;
        }
        return CATEGORY_SHOPPING;
    }

    private String getCategoryLabel(int category) {
        switch (category) {
            case CATEGORY_FOOD:
                return getString(R.string.category_food);
            case CATEGORY_TRANSPORT:
                return getString(R.string.category_transport);
            case CATEGORY_SHOPPING:
            default:
                return getString(R.string.category_shopping);
        }
    }

    private double getDoubleFromResponse(JSONObject response, String key) {
        Object value = response.opt(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private void clearResults() {
        tvPredictionResult.setText(R.string.prediction_placeholder);
        tvExpectedSavings.setText(R.string.prediction_placeholder);
        tvSmartSuggestion.setText(R.string.prediction_placeholder);
    }

    private String getText(TextInputEditText inputEditText) {
        return inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
    }

    private void prefillCurrentMonthExpenseFields() {
        if (databaseHelper == null) {
            return;
        }

        double foodTotal = databaseHelper.getCurrentMonthExpenseByCategory("Food");
        double transportTotal = databaseHelper.getCurrentMonthExpenseByCategory("Transport");
        double shoppingTotal = databaseHelper.getCurrentMonthExpenseByCategory("Shopping");

        boolean didAutofill = false;
        didAutofill |= setIfEmpty(etFood, foodTotal);
        didAutofill |= setIfEmpty(etTransport, transportTotal);
        didAutofill |= setIfEmpty(etShopping, shoppingTotal);

        if (tvAutofillInfo != null) {
            tvAutofillInfo.setVisibility(didAutofill ? View.VISIBLE : View.GONE);
        }
    }

    private boolean setIfEmpty(TextInputEditText inputEditText, double value) {
        if (!TextUtils.isEmpty(getText(inputEditText))) {
            return false;
        }
        inputEditText.setText(formatAmount(value));
        return true;
    }

    private String formatAmount(double amount) {
        return String.format(Locale.getDefault(), "%.2f", amount);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }

}
