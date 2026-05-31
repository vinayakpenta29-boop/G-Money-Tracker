package com.example.moneytracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        super(context, 0, transactions);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transaction transaction = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        }

        TextView tvPerson = convertView.findViewById(R.id.tvPerson);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvAmount = convertView.findViewById(R.id.tvAmount);

        tvPerson.setText(transaction.getPerson());
        tvDate.setText(transaction.getDate()); 
        
        if (transaction.getType().equals("owes_me")) {
            tvAmount.setText("+$" + String.format("%.2f", transaction.getAmount()));
            tvAmount.setTextColor(Color.parseColor("#2ecc71")); 
        } else {
            tvAmount.setText("-$" + String.format("%.2f", transaction.getAmount()));
            tvAmount.setTextColor(Color.parseColor("#e74c3c")); 
        }

        return convertView;
    }
}
