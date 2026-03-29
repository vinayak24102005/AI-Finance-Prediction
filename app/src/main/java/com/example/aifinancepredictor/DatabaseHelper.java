package com.example.aifinancepredictor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // create sqlite database helper for finance app
// create table transactions with columns:
// id, amount, category, date, type (income/expense)
// implement methods:
// insertTransaction(amount, category, date, type)
// getAllTransactions()
// getTotalIncome()
// getTotalExpense()
// return results using cursor
// use SQLiteOpenHelper
    private static final String DATABASE_NAME = "FinanceDB";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_EXPENSES = "expenses";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "email TEXT UNIQUE NOT NULL,"
                + "password TEXT NOT NULL,"
                + "security_code TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "amount REAL NOT NULL,"
                + "category TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "type TEXT NOT NULL DEFAULT 'expense')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN security_code TEXT NOT NULL DEFAULT '0000'");
        }
    }

    public boolean insertUser(String name, String email, String password, String securityCode) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("security_code", securityCode);
        return db.insert(TABLE_USERS, null, values) != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE email = ? AND password = ? LIMIT 1",
                new String[]{email, password}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkSecurityCode(String email, String securityCode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE email = ? AND security_code = ? LIMIT 1",
                new String[]{email, securityCode}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean insertExpense(double amount, String category, String date, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("type", type);
        return db.insert(TABLE_EXPENSES, null, values) != -1;
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, amount, category, date, type FROM " + TABLE_EXPENSES + " ORDER BY date DESC, id DESC",
                null
        );
    }

    public Cursor getExpensesByMonth(String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, amount, category, date, type FROM " + TABLE_EXPENSES
                        + " WHERE date LIKE ? ORDER BY date DESC, id DESC",
                new String[]{yearMonth + "-%"}
        );
    }

    public Cursor getExpensesByDateRange(String fromDate, String toDate) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, amount, category, date, type FROM " + TABLE_EXPENSES
                        + " WHERE date >= ? AND date <= ? ORDER BY date DESC, id DESC",
                new String[]{fromDate, toDate}
        );
    }

    public Cursor getExpensesByDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, amount, category, date, type FROM " + TABLE_EXPENSES
                        + " WHERE date = ? ORDER BY date DESC, id DESC",
                new String[]{date}
        );
    }

    public double getTotalIncome() {
        return getTotalByType("income");
    }

    public double getTotalExpense() {
        return getTotalByType("expense");
    }

    public double getTotalIncomeByDate(String date) {
        return getTotalByTypeAndDate("income", date);
    }

    public double getTotalExpenseByDate(String date) {
        return getTotalByTypeAndDate("expense", date);
    }

    public double getTotalIncomeByMonth(String yearMonth) {
        return getTotalByTypeAndMonth("income", yearMonth);
    }

    public double getTotalExpenseByMonth(String yearMonth) {
        return getTotalByTypeAndMonth("expense", yearMonth);
    }

    public double getCurrentMonthExpenseByCategory(String category) {
        String currentYearMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        return getExpenseTotalByCategoryAndMonth(category, currentYearMonth);
    }

    public double getExpenseTotalByCategoryAndMonth(String category, String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_EXPENSES
                        + " WHERE type = ? AND LOWER(category) = LOWER(?) AND date LIKE ?",
                new String[]{"expense", category, yearMonth + "-%"}
        );
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    private double getTotalByType(String type) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_EXPENSES + " WHERE type = ?",
                new String[]{type}
        );
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    private double getTotalByTypeAndDate(String type, String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_EXPENSES + " WHERE type = ? AND date = ?",
                new String[]{type, date}
        );
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    private double getTotalByTypeAndMonth(String type, String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_EXPENSES + " WHERE type = ? AND date LIKE ?",
                new String[]{type, yearMonth + "-%"}
        );
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
}
