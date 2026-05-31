package com.example.moneytracker;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MemberActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String personName;
    private TextView tvTitle, tvStatementSummary;
    private ListView listView;
    private List<Transaction> memberTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        dbHelper = new DatabaseHelper(this);
        personName = getIntent().getStringExtra("PERSON_NAME");

        tvTitle = findViewById(R.id.tvMemberTitle);
        tvStatementSummary = findViewById(R.id.tvStatementSummary);
        listView = findViewById(R.id.lvMemberTransactions);

        tvTitle.setText("Account Statement: " + personName);
        
        loadMemberData();
    }

    private void loadMemberData() {
        memberTransactions = dbHelper.getTransactionsByPerson(personName);
        TransactionAdapter adapter = new TransactionAdapter(this, memberTransactions);
        listView.setAdapter(adapter);
        
        calculateMemberStatement();
    }

    private void calculateMemberStatement() {
        double totalOwedToMe = 0;
        double totalIOweThem = 0;

        for (Transaction t : memberTransactions) {
            if (t.getType().equals("owes_me")) {
                totalOwedToMe += t.getAmount();
            } else {
                totalIOweThem += t.getAmount();
            }
        }

        double netBalance = totalOwedToMe - totalIOweThem;
        
        String summary = "Total they owe you: $" + String.format("%.2f", totalOwedToMe) + "\n" +
                         "Total you paid them: $" + String.format("%.2f", totalIOweThem) + "\n\n";

        if (netBalance > 0) {
            summary += "CURRENT BALANCE: They owe you $" + String.format("%.2f", netBalance);
        } else if (netBalance < 0) {
            summary += "CURRENT BALANCE: You owe them $" + String.format("%.2f", Math.abs(netBalance));
        } else {
            summary += "CURRENT BALANCE: Settled ($0.00)";
        }

        tvStatementSummary.setText(summary);
    }

    public void deleteTransaction(int id) {
        dbHelper.deleteTransaction(id);
        loadMemberData(); // Reload UI
    }

    private void loadMemberData() {
        memberTransactions = dbHelper.getTransactionsByPerson(personName);
        TransactionAdapter adapter = new TransactionAdapter(this, memberTransactions);
        listView.setAdapter(adapter);
        
        calculateMemberStatement();

        // LONG CLICK LISTENER FOR MEMBER SCREEN
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaction t = memberTransactions.get(position);
            
            new android.app.AlertDialog.Builder(MemberActivity.this)
                .setTitle("Delete Transaction")
                .setMessage("Delete this $" + t.getAmount() + " record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(t.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
                
            return true;
        });
    }
}
