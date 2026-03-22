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

    // create prediction activity
// take inputs: food, transport, shopping
// validate inputs
// send POST request to API
// use deployed https URL
// show loading message
// parse response (status, predicted_expense)
// display result
// handle errors properly
    private static final String PREDICTION_URL = "https://finance-api-254i.onrender.com/predict";

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

    private RequestQueue requestQueue;

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

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        btnPredict.setOnClickListener(v -> fetchPrediction());
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

        tvPredictionResult.setText(String.format(Locale.getDefault(), "Rs %.2f", predictedExpense));

        String savingsText;
        int savingsColor;
        if (savings >= 0) {
            savingsText = String.format(Locale.getDefault(), "Rs %.2f (Remaining Budget)", savings);
            savingsColor = R.color.status_low;
        } else {
            savingsText = String.format(Locale.getDefault(), "Rs %.2f (Over Budget)", Math.abs(savings));
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
        if (savings < 0) {
            return String.format(
                    Locale.getDefault(),
                    "Overspending alert: projected spend exceeds your budget by Rs %.2f. Cut down on %s first.",
                    Math.abs(savings),
                    getHighestCategory(food, transport, shopping)
            );
        }

        String highestCategory = getHighestCategory(food, transport, shopping);
        if ("Food".equals(highestCategory)) {
            return "Food is your highest expense. Try meal planning and fewer impulse orders.";
        }
        if ("Transport".equals(highestCategory)) {
            return "Transport is your highest expense. Consider pooling or monthly travel passes.";
        }
        if ("Shopping".equals(highestCategory)) {
            return "Shopping is your highest expense. Delay non-essential purchases for 24 hours.";
        }

        if (budget > 0 && predictedExpense >= (budget * 0.8)) {
            return "Your predicted spend is close to your budget limit. Keep a strict budget this month.";
        }

        return "You are on track. Continue tracking daily to improve savings.";
    }

    private String getHighestCategory(double food, double transport, double shopping) {
        if (food >= transport && food >= shopping) {
            return "Food";
        }
        if (transport >= food && transport >= shopping) {
            return "Transport";
        }
        return "Shopping";
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

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }

}
