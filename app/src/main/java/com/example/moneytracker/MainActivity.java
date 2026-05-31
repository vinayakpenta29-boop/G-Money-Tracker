package com.example.moneytracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvOwedToMe, tvIOwe, tvNetBalance;
    private EditText etPerson, etAmount;
    private Spinner spinnerType;
    private ListView listView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvOwedToMe = findViewById(R.id.tvOwedToMe);
        tvIOwe = findViewById(R.id.tvIOwe);
        tvNetBalance = findViewById(R.id.tvNetBalance);
        etPerson = findViewById(R.id.etPerson);
        etAmount = findViewById(R.id.etAmount);
        spinnerType = findViewById(R.id.spinnerType);
        Button btnAdd = findViewById(R.id.btnAdd);
        listView = findViewById(R.id.listView);

        String[] types = {"They owe me", "I owe them"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(spinnerAdapter);

        btnAdd.setOnClickListener(v -> addRecord());

        // FIXED: Restored the normal click listener to open the Member Ledger screen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Transaction t = transactionList.get(position);
            Intent intent = new Intent(MainActivity.this, MemberActivity.class);
            intent.putExtra("PERSON_NAME", t.getPerson());
            startActivity(intent);
        });

        // LONG CLICK LISTENER: Tap and hold to delete a transaction
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaction t = transactionList.get(position);
            
            new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete Transaction")
                .setMessage("Delete this $" + t.getAmount() + " record for " + t.getPerson() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(t.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
                
            return true; // Tells Android we handled the long click
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Reload data when coming back from Member screen
    }

    private void addRecord() {
        String person = etPerson.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (person.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = spinnerType.getSelectedItemPosition() == 0 ? "owes_me" : "i_owe";
        
        // Auto-generate today's date
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        boolean success = dbHelper.addTransaction(person, amount, type, currentDate);
        if (success) {
            etPerson.setText("");
            etAmount.setText("");
            loadData();
        } else {
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteTransaction(int id) {
        dbHelper.deleteTransaction(id);
        loadData();
    }

    private void loadData() {
        transactionList = dbHelper.getAllTransactions();
        adapter = new TransactionAdapter(this, transactionList);
        listView.setAdapter(adapter);
        calculateTotals();
    }

    private void calculateTotals() {
        double owedToMe = 0, iOwe = 0;
        for (Transaction t : transactionList) {
            if (t.getType().equals("owes_me")) owedToMe += t.getAmount();
            else iOwe += t.getAmount();
        }
        tvOwedToMe.setText("Owed to me:\n$" + String.format("%.2f", owedToMe));
        tvIOwe.setText("I owe:\n$" + String.format("%.2f", iOwe));
        tvNetBalance.setText("Net Balance:\n$" + String.format("%.2f", (owedToMe - iOwe)));
    }
}
