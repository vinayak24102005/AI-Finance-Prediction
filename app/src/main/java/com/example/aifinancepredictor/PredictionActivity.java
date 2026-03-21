package com.example.aifinancepredictor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private Button btnPredict;
    private ProgressBar progressPrediction;
    private TextView tvPredictionStatus;
    private TextView tvPredictionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_prediction);

        etFood = findViewById(R.id.etFood);
        etTransport = findViewById(R.id.etTransport);
        etShopping = findViewById(R.id.etShopping);
        btnPredict = findViewById(R.id.btnPredict);
        progressPrediction = findViewById(R.id.progressPrediction);
        tvPredictionStatus = findViewById(R.id.tvPredictionStatus);
        tvPredictionResult = findViewById(R.id.tvPredictionResult);

        btnPredict.setOnClickListener(v -> fetchPrediction());
    }

    private void fetchPrediction() {
        String foodText = getText(etFood);
        String transportText = getText(etTransport);
        String shoppingText = getText(etShopping);

        if (TextUtils.isEmpty(foodText) || TextUtils.isEmpty(transportText) || TextUtils.isEmpty(shoppingText)) {
            Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
            return;
        }

        double food;
        double transport;
        double shopping;
        try {
            food = Double.parseDouble(foodText);
            transport = Double.parseDouble(transportText);
            shopping = Double.parseDouble(shoppingText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (food < 0 || transport < 0 || shopping < 0) {
            Toast.makeText(this, "Values cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("food", food);
            requestBody.put("transport", transport);
            requestBody.put("shopping", shopping);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to prepare request", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true, "Predicting...");
        tvPredictionResult.setText("");
        runPredictionRequest(requestBody);
    }

    private void runPredictionRequest(JSONObject requestBody) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(PREDICTION_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setDoOutput(true);

                byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload);
                outputStream.flush();
                outputStream.close();

                int statusCode = connection.getResponseCode();
                InputStream responseStream = statusCode >= 200 && statusCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String responseText = readStream(responseStream);

                if (statusCode < 200 || statusCode >= 300) {
                    String errorText = "Prediction failed (HTTP " + statusCode + ")";
                    runOnUiThread(() -> {
                        setLoading(false, "");
                        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                JSONObject response = new JSONObject(responseText);
                String status = response.optString("status", "");
                double predictedExpense = response.optDouble("predicted_expense", Double.NaN);
                if (Double.isNaN(predictedExpense)) {
                    predictedExpense = response.optDouble("prediction", Double.NaN);
                }

                if (Double.isNaN(predictedExpense)) {
                    runOnUiThread(() -> {
                        setLoading(false, "");
                        tvPredictionResult.setText("Prediction key is missing in API response.");
                    });
                    return;
                }

                double finalPredictedExpense = predictedExpense;
                String finalStatus = status;
                runOnUiThread(() -> {
                    setLoading(false, "");
                    if (!TextUtils.isEmpty(finalStatus)) {
                        tvPredictionStatus.setText("Status: " + finalStatus);
                    }
                    tvPredictionResult.setText(
                            String.format(Locale.getDefault(), "Predicted Expense: Rs %.2f", finalPredictedExpense)
                    );
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false, "");
                    Toast.makeText(this, "Prediction failed. Check API server URL.", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void setLoading(boolean loading, String message) {
        btnPredict.setEnabled(!loading);
        progressPrediction.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvPredictionStatus.setText(message);
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private String getText(TextInputEditText inputEditText) {
        return inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
    }

}

