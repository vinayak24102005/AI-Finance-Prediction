# AI Finance Predictor (Android)

AI Finance Predictor is a Java Android app that tracks income and expenses locally with SQLite and provides a prediction screen powered by a remote ML API.

## Features

- User registration and login (SQLite-backed)
- 4-digit security code verification after login
- Session handling using SharedPreferences
- Add transactions with amount, type, category, and date
- Dashboard showing:
  - Today's total income
  - Today's total expense
  - Overall balance
  - Today's transactions list
- Transaction history with date-range filter
- Income vs expense pie chart using MPAndroidChart
- Expense prediction via HTTP POST request to a remote endpoint

## Tech Stack

- Language: Java 11
- Platform: Android
- SDK levels: minSdk 29, targetSdk 36, compileSdk 36
- Local database: SQLite (`SQLiteOpenHelper`)
- UI: AndroidX + Material Components
- Charting: MPAndroidChart

## Project Structure

- App module: `app/`
- Main package: `app/src/main/java/com/example/aifinancepredictor/`
- Manifest: `app/src/main/AndroidManifest.xml`

### Main Activities

- `LoginActivity` - login screen (launcher)
- `RegisterActivity` - user signup
- `SecurityCodeActivity` - second-step verification
- `DashboardActivity` - main home screen after auth
- `AddExpenseActivity` - add income/expense transactions
- `HistoryActivity` - transaction list + filtering
- `ChartActivity` - pie chart analysis
- `PredictionActivity` - API-based prediction

### Core Helpers

- `DatabaseHelper` - table creation and SQLite operations
- `AuthSessionManager` - login/session state in SharedPreferences

## Database Schema

### `users`

- `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `name` (TEXT NOT NULL)
- `email` (TEXT UNIQUE NOT NULL)
- `password` (TEXT NOT NULL)
- `security_code` (TEXT NOT NULL)

### `expenses`

- `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `amount` (REAL NOT NULL)
- `category` (TEXT NOT NULL)
- `date` (TEXT NOT NULL)
- `type` (TEXT NOT NULL, default `expense`)

## Prediction API (Flask API)

`PredictionActivity` calls:

- URL: `https://finance-api-254i.onrender.com/predict`
- Method: `POST`
- Content-Type: `application/json`
- Request fields:
  - `food`
  - `transport`
  - `shopping`

The app accepts either response key:

- `predicted_expense`
- `prediction`

Example request body:

```json
{
  "food": 1200,
  "transport": 600,
  "shopping": 900
}
```

## Run the App

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the `app` module on an emulator or physical device.
4. Register a new account.
5. Login and verify the 4-digit security code.
6. Use Dashboard to navigate to Add, History, Chart, and Prediction screens.

## Notes

- Internet permission is required and already declared in `AndroidManifest.xml`.
- Data is stored locally on device using SQLite.
- Current password storage is plain text; upgrading to hashed storage is recommended.
