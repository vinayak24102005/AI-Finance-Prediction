package com.example.aifinancepredictor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    // create recycler view adapter
// bind transaction data
// show income in green and expense in red
// display type label
    private final List<ExpenseItem> expenses;

    public ExpenseAdapter(List<ExpenseItem> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseItem item = expenses.get(position);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "Rs %.2f", item.getAmount()));
        holder.tvCategory.setText(item.getCategory());
        holder.tvDate.setText(item.getDate());

        String type = item.getType();
        boolean isIncome = "income".equalsIgnoreCase(type);
        holder.tvType.setText(isIncome ? "Income" : "Expense");

        int color = ContextCompat.getColor(
                holder.itemView.getContext(),
                isIncome ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
        );
        holder.tvAmount.setTextColor(color);
        holder.tvType.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvAmount;
        TextView tvCategory;
        TextView tvDate;
        TextView tvType;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvType = itemView.findViewById(R.id.tvItemType);
        }
    }
}
