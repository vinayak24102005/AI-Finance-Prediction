# AI Finance Predictor (Android)

AI Finance Predictor is a Java Android app that tracks income and expenses locally with SQLite and provides a prediction screen powered by a remote ML API with full Material3 theme support (light & dark modes).

## Features

- **User Authentication**
  - User registration and login (SQLite-backed)
  - 4-digit security code verification after login
  - Session handling using SharedPreferences

- **Transaction Management**
  - Add transactions with amount, type, category, and date
  - Dashboard showing today's income, expense, and overall balance
  - Transaction history with date-range filter

- **Analytics & Visualization**
  - Income vs expense pie chart using MPAndroidChart
  - Expense prediction via Volley HTTP POST to remote ML API

- **Prediction Screen (Smart AI-Powered)**
  - Input food, transport, and shopping expenses
  - Sends JSON POST request to ML API
  - Parses `predicted_expense` from response
  - Calculates expected savings (income - predicted)
  - Classifies expense level with color-coded status:
    - **Low** (< Rs 2000) - Green
    - **Moderate** (Rs 2000–4000) - Amber
    - **High** (> Rs 4000) - Red
  - Generates smart suggestions based on:
    - Overspending alert (if savings < 0)
    - Highest expense category
    - Savings buffer vs income ratio
  - Shows progress indicator during API call
  - 30-second timeout with automatic retry

- **Theme Support**
  - Full Material3 DayNight theme support
  - Automatic light/dark mode detection
  - Proper color contrast and readability in both modes
  - All hardcoded strings moved to `strings.xml`

## Tech Stack

- **Language:** Java 11
- **Platform:** Android
- **SDK Levels:** minSdk 29, targetSdk 36, compileSdk 36
- **Local Database:** SQLite (`SQLiteOpenHelper`)
- **UI Framework:** AndroidX + Material3 Components
- **Networking:** Volley (JSON requests with retry policy)
- **Charting:** MPAndroidChart

## Project Structure

```
app/src/main/
├── java/com/example/aifinancepredictor/
│   ├── LoginActivity
│   ├── RegisterActivity
│   ├── SecurityCodeActivity
│   ├── DashboardActivity
│   ├── AddExpenseActivity
│   ├── HistoryActivity
│   ├── ChartActivity
│   ├── PredictionActivity          # ML-powered prediction screen
│   ├── DatabaseHelper
│   └── AuthSessionManager
├── res/
│   ├── layout/
│   │   ├── activity_prediction.xml  # Material3 ScrollView-based layout
│   │   ├── ...other layouts
│   ├── values/
│   │   ├── colors.xml              # Light theme Material3 colors
│   │   ├── themes.xml              # Light theme style definitions
│   │   └── strings.xml             # All UI text resources
│   ├── values-night/
│   │   ├── colors.xml              # Dark theme Material3 colors
│   │   ├── themes.xml              # Dark theme style definitions
│   │   └── strings.xml             # Dark theme text resources
│   └── AndroidManifest.xml
└── AndroidManifest.xml
```

### Core Activities

| Activity | Purpose |
|----------|---------|
| `LoginActivity` | Email/password login (launcher) |
| `RegisterActivity` | New user signup |
| `SecurityCodeActivity` | 2FA verification |
| `DashboardActivity` | Home screen with balance summary |
| `AddExpenseActivity` | Add income/expense transactions |
| `HistoryActivity` | Transaction list with filtering |
| `ChartActivity` | Income vs expense visualization |
| `PredictionActivity` | AI-powered expense forecasting |

### Core Helpers

| Class | Purpose |
|-------|---------|
| `DatabaseHelper` | SQLite CRUD operations |
| `AuthSessionManager` | Login/session state management |

## Prediction Activity API Contract

### Request
```json
{
  "food": 500.0,
  "transport": 1000.0,
  "shopping": 2000.0
}
```

### Response (Expected)
```json
{
  "status": "success",
  "predicted_expense": 3500.0
}
```

### Processing Flow
1. **Input Validation**
   - Check for empty fields
   - Validate numeric values
   - Reject negative amounts

2. **API Call**
   - POST to `https://finance-api-254i.onrender.com/predict`
   - Content-Type: `application/json`
   - Timeout: 30 seconds with automatic retry

3. **Response Parsing**
   - Extract `predicted_expense` (or fallback to `prediction`)
   - Fetch total income from local database
   - Calculate savings: `income - predicted_expense`

4. **Status Classification**
   ```
   if predicted < 2000:  → Low (Green)
   else if predicted ≤ 4000: → Moderate (Amber)
   else: → High (Red)
   ```

5. **Smart Suggestion Generation**
   ```
   if savings < 0:
     → "Overspending alert: exceed by Rs X. Cut down on [Food/Transport/Shopping]"
   else if highest_category == Food:
     → "Food is highest. Try meal planning..."
   else if highest_category == Transport:
     → "Transport is highest. Consider pooling..."
   else if highest_category == Shopping:
     → "Shopping is highest. Delay non-essential..."
   else if predicted ≥ income × 0.8:
     → "Spend close to income. Keep strict budget..."
   else:
     → "On track. Continue tracking daily..."
   ```

## Theme Architecture

### Color System (Material3)
- **Primary:** Purple (#6750A4 light, #D0BCFF dark)
- **Secondary:** Cool Gray (#625B71 light, #CCC7DB dark)
- **Tertiary:** Rose (#7D5260 light, #F1B6DA dark)
- **Error:** Red (#DC2626 light, #F87171 dark)

### Status Colors
- **Pending:** Gray (#64748B light, #94A3B8 dark)
- **Low Expense:** Green (#059669 light, #4ADE80 dark)
- **Moderate Expense:** Amber (#D97706 light, #FBBF24 dark)
- **High Expense:** Red (#DC2626 light, #F87171 dark)

### Dynamic Color References
All layouts use theme attributes instead of hardcoded colors:
- `?attr/colorSurface` - Background
- `?attr/colorOnSurface` - Text
- `?attr/colorOutlineVariant` - Borders
- `@color/status_low/moderate/high` - Semantic status colors

## Prediction Screen UI

### Layout Components
1. **Input Section (Material Card)**
   - Food Expense (EditText with icon)
   - Transport Expense (EditText with icon)
   - Shopping Expense (EditText with icon)
   - Predict Button (rounded MaterialButton)
   - Progress indicator (hidden by default)

2. **Result Section (Material Card)**
   - Predicted Expense (bold primary text)
   - Expected Savings (with income calculation)
   - Expense Status (color-coded level label)
   - Smart Suggestion (actionable advice)

### Styling
- ScrollView for overflow content
- Material3 OutlinedBox TextInputLayout
- Material3 MaterialButton with 18dp corner radius
- 16dp card corner radius with subtle elevation
- 20dp padding, 12-18dp spacing
- Theme-aware colors for light/dark modes

## String Resources

All UI text uses `strings.xml` for easy localization:
- Prediction activity labels
- Validation error messages
- Status labels
- API error messaging
- Button text and hints

Both `values/strings.xml` (light) and `values-night/strings.xml` (dark) are maintained.

## Dependencies

### From `libs.versions.toml` / `build.gradle.kts`
```
androidX.appcompat          1.7.1
androidX.material           1.13.0
androidX.activity           1.13.0
androidX.constraintlayout   2.2.1
androidX.recyclerview       1.4.0
com.android.volley:volley   1.2.1
com.github.PhilJay.MPAndroidChart  v3.1.0
```

## How to Build & Run

```bash
# Clone repository
git clone <repo-url>
cd AI-Finance-Prediction

# Build debug APK
./gradlew assembleDebug

# Run on emulator/device
./gradlew installDebug

# Run tests
./gradlew testDebug
```

### Run on a Small Phone Emulator

Use this once to add a compact test device profile:

1. Open **Android Studio** -> **Device Manager**.
2. Click **Create device**.
3. Select **Phone** and choose a small profile (or create custom):
   - Screen size: **4.0"**
   - Resolution: **720 x 1280**
   - RAM: **1536 MB** (or default)
4. Select a system image (API 34+ recommended) and finish.
5. Start the emulator and run the `app` configuration.

Optional Windows PowerShell CLI flow:

```powershell
sdkmanager "system-images;android-34;google_apis;x86_64"
avdmanager create avd -n SmallPhone_API34 -k "system-images;android-34;google_apis;x86_64" -d "pixel_4"
emulator -avd SmallPhone_API34
.\gradlew.bat installDebug
```

If `avdmanager` asks for a custom hardware profile, press **Enter** to accept defaults.

## API Endpoints

### Prediction API
- **URL:** `https://finance-api-254i.onrender.com/predict`
- **Method:** POST
- **Content-Type:** application/json
- **Timeout:** 30 seconds
- **Retry Policy:** DefaultRetryPolicy with automatic backoff

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
