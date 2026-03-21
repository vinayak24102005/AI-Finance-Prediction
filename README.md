# AI Finance Predictor (Android)

A Java Android app with local SQLite expense tracking plus a Flask-backed prediction screen.

## Implemented Features

- Login/register using SQLite (`users` table)
- Dashboard available only after login
- Add expense screen (`amount`, `category`, `date`)
- Expense history with RecyclerView
- Expense analysis summary by category totals and percentages
- Prediction screen using HTTP POST request to `http://10.0.2.2:5000/predict`

## Project Notes

- Database helper: `app/src/main/java/com/example/aifinancepredictor/DatabaseHelper.java`
- Main launcher activity: `LoginActivity`
- Dashboard activity: `MainActivity`

## Quick Try

1. Open in Android Studio / JetBrains Android plugin.
2. Sync Gradle.
3. Run on emulator.
4. Register -> Login -> Dashboard.
5. Add expenses, then check History and Chart.
6. Start Flask server for prediction endpoint, then test Prediction screen.

## Flask Response Format

The app accepts either key in JSON response:

- `predicted_expense`
- `prediction`

