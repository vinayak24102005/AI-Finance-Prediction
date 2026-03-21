package com.example.aifinancepredictor;

public class ExpenseItem {

    private final double amount;
    private final String category;
    private final String date;
    private final String type;

    public ExpenseItem(double amount, String category, String date, String type) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }
}

