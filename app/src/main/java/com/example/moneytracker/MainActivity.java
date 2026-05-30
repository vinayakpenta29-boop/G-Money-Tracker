package com.example.moneytracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

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

        // Initialize UI components
        tvOwedToMe = findViewById(R.id.tvOwedToMe);
        tvIOwe = findViewById(R.id.tvIOwe);
        tvNetBalance = findViewById(R.id.tvNetBalance);
        etPerson = findViewById(R.id.etPerson);
        etAmount = findViewById(R.id.etAmount);
        spinnerType = findViewById(R.id.spinnerType);
        Button btnAdd = findViewById(R.id.btnAdd);
        listView = findViewById(R.id.listView);

        // Set up Spinner (Dropdown)
        String[] types = {"They owe me", "I owe them"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(spinnerAdapter);

        // Add Button Listener
        btnAdd.setOnClickListener(v -> addRecord());

        loadData();
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

        boolean success = dbHelper.addTransaction(person, amount, type);
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
        double owedToMe = 0;
        double iOwe = 0;

        for (Transaction t : transactionList) {
            if (t.getType().equals("owes_me")) {
                owedToMe += t.getAmount();
            } else {
                iOwe += t.getAmount();
            }
        }

        tvOwedToMe.setText("Owed to me:\n$" + String.format("%.2f", owedToMe));
        tvIOwe.setText("I owe:\n$" + String.format("%.2f", iOwe));
        tvNetBalance.setText("Net Balance:\n$" + String.format("%.2f", (owedToMe - iOwe)));
    }
}
