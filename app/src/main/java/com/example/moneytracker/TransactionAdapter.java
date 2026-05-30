package com.example.moneytracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private MainActivity activity;

    public TransactionAdapter(MainActivity context, List<Transaction> transactions) {
        super(context, 0, transactions);
        this.activity = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transaction transaction = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        }

        TextView tvPerson = convertView.findViewById(R.id.tvPerson);
        TextView tvAmount = convertView.findViewById(R.id.tvAmount);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        tvPerson.setText(transaction.getPerson());
        
        if (transaction.getType().equals("owes_me")) {
            tvAmount.setText("+$" + String.format("%.2f", transaction.getAmount()));
            tvAmount.setTextColor(Color.parseColor("#2ecc71")); // Green
        } else {
            tvAmount.setText("-$" + String.format("%.2f", transaction.getAmount()));
            tvAmount.setTextColor(Color.parseColor("#e74c3c")); // Red
        }

        btnDelete.setOnClickListener(v -> activity.deleteTransaction(transaction.getId()));

        return convertView;
    }
}
