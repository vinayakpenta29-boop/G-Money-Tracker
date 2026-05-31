package com.example.moneytracker;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // Tabs
    private TextView tabMembers, tabTransactions, tabSettlement;
    private LinearLayout layoutMembers, layoutTransactions;
    private View layoutSettlement; // ScrollView

    // Tab 1: Members
    private EditText etNewMemberName;

    // Tab 2: Transactions
    private Spinner spinnerMembers, spinnerType;
    private EditText etAmount;
    private ListView listViewTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    // Tab 3: Settlement
    private TableLayout tableOweMe, tableIOwe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupTabs();
        loadMembersIntoSpinner();
        loadTransactions();

        // Add Member Button
        findViewById(R.id.btnAddMember).setOnClickListener(v -> addMember());

        // Add Transaction Button
        findViewById(R.id.btnAddTransaction).setOnClickListener(v -> addTransaction());

        // Transaction Click -> Show Popup
        listViewTransactions.setOnItemClickListener((parent, view, position, id) -> {
            Transaction t = transactionList.get(position);
            showMemberLedgerPopup(t.getPerson());
        });

        // Transaction Long Click -> Delete
        listViewTransactions.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaction t = transactionList.get(position);
            new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete this $" + t.getAmount() + " record?")
                .setPositiveButton("Yes", (d, w) -> deleteTransaction(t.getId()))
                .setNegativeButton("No", null)
                .show();
            return true;
        });
    }

    private void initViews() {
        tabMembers = findViewById(R.id.tabMembers);
        tabTransactions = findViewById(R.id.tabTransactions);
        tabSettlement = findViewById(R.id.tabSettlement);

        layoutMembers = findViewById(R.id.layoutMembers);
        layoutTransactions = findViewById(R.id.layoutTransactions);
        layoutSettlement = findViewById(R.id.layoutSettlement);

        etNewMemberName = findViewById(R.id.etNewMemberName);
        spinnerMembers = findViewById(R.id.spinnerMembers);
        etAmount = findViewById(R.id.etAmount);
        spinnerType = findViewById(R.id.spinnerType);
        listViewTransactions = findViewById(R.id.listViewTransactions);

        tableOweMe = findViewById(R.id.tableOweMe);
        tableIOwe = findViewById(R.id.tableIOwe);

        String[] types = {"They owe me", "I owe them"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(spinnerAdapter);
    }

    private void setupTabs() {
        tabMembers.setOnClickListener(v -> switchTab(0));
        tabTransactions.setOnClickListener(v -> switchTab(1));
        tabSettlement.setOnClickListener(v -> switchTab(2));
    }

    private void switchTab(int index) {
        // Reset colors
        tabMembers.setBackgroundColor(Color.TRANSPARENT);
        tabTransactions.setBackgroundColor(Color.TRANSPARENT);
        tabSettlement.setBackgroundColor(Color.TRANSPARENT);

        layoutMembers.setVisibility(View.GONE);
        layoutTransactions.setVisibility(View.GONE);
        layoutSettlement.setVisibility(View.GONE);

        String activeColor = "#E0E0E0"; // Light grey for active tab like a toggle

        if (index == 0) {
            tabMembers.setBackgroundColor(Color.parseColor(activeColor));
            layoutMembers.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            tabTransactions.setBackgroundColor(Color.parseColor(activeColor));
            layoutTransactions.setVisibility(View.VISIBLE);
            loadTransactions();
        } else if (index == 2) {
            tabSettlement.setBackgroundColor(Color.parseColor(activeColor));
            layoutSettlement.setVisibility(View.VISIBLE);
            loadSettlementTables();
        }
    }

    // --- TAB 1 LOGIC ---
    private void addMember() {
        String name = etNewMemberName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.addMember(name)) {
            Toast.makeText(this, "Member Added", Toast.LENGTH_SHORT).show();
            etNewMemberName.setText("");
            loadMembersIntoSpinner(); // Refresh spinner
        } else {
            Toast.makeText(this, "Member might already exist", Toast.LENGTH_SHORT).show();
        }
    }

    // --- TAB 2 LOGIC ---
    private void loadMembersIntoSpinner() {
        List<String> members = dbHelper.getAllMembers();
        members.add(0, "--Select Member--"); // Default option at the top
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, members);
        spinnerMembers.setAdapter(adapter);
    }

    private void addTransaction() {
        if (spinnerMembers.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }
        String person = spinnerMembers.getSelectedItem().toString();
        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = spinnerType.getSelectedItemPosition() == 0 ? "owes_me" : "i_owe";
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        if (dbHelper.addTransaction(person, amount, type, date)) {
            etAmount.setText("");
            loadTransactions();
        }
    }

    private void loadTransactions() {
        transactionList = dbHelper.getAllTransactions();
        adapter = new TransactionAdapter(this, transactionList);
        listViewTransactions.setAdapter(adapter);
    }

    public void deleteTransaction(int id) {
        dbHelper.deleteTransaction(id);
        loadTransactions();
        loadSettlementTables(); // Refresh tables if open
    }

    private void showMemberLedgerPopup(String personName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ledger, null);
        builder.setView(view);

        TextView tvSummary = view.findViewById(R.id.tvPopupSummary);
        ListView lvPopup = view.findViewById(R.id.lvPopupTransactions);

        List<Transaction> memTrans = dbHelper.getTransactionsByPerson(personName);
        lvPopup.setAdapter(new TransactionAdapter(this, memTrans));

        double owedToMe = 0, iOweThem = 0;
        for (Transaction t : memTrans) {
            if (t.getType().equals("owes_me")) owedToMe += t.getAmount();
            else iOweThem += t.getAmount();
        }
        double net = owedToMe - iOweThem;
        
        String sumText = "Total Taken: $" + String.format("%.2f", owedToMe) + 
                         "\nTotal Paid: $" + String.format("%.2f", iOweThem) + 
                         "\nBalance: " + (net >= 0 ? "They owe $" + String.format("%.2f", net) : "You owe $" + String.format("%.2f", Math.abs(net)));
        tvSummary.setText(sumText);

        builder.setTitle("Ledger: " + personName);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // --- TAB 3 LOGIC (SETTLEMENT) ---
    private void loadSettlementTables() {
        tableOweMe.removeAllViews();
        tableIOwe.removeAllViews();
        addHeaderRow(tableOweMe);
        addHeaderRow(tableIOwe);

        List<String> members = dbHelper.getAllMembers();
        
        double t1Taken = 0, t1Paid = 0, t1Bal = 0;
        double t2Taken = 0, t2Paid = 0, t2Bal = 0;

        for (String member : members) {
            List<Transaction> trans = dbHelper.getTransactionsByPerson(member);
            if (trans.isEmpty()) continue;

            double owesMe = 0, iOwe = 0;
            for (Transaction t : trans) {
                if (t.getType().equals("owes_me")) owesMe += t.getAmount();
                else iOwe += t.getAmount();
            }

            double net = owesMe - iOwe;

            if (net > 0) { // They owe me
                addDataRow(tableOweMe, member, owesMe, iOwe, net);
                t1Taken += owesMe; t1Paid += iOwe; t1Bal += net;
            } else if (net < 0) { // I owe them
                addDataRow(tableIOwe, member, iOwe, owesMe, Math.abs(net));
                t2Taken += iOwe; t2Paid += owesMe; t2Bal += Math.abs(net);
            }
        }

        // Add Grand Totals
        addTotalRow(tableOweMe, t1Taken, t1Paid, t1Bal);
        addTotalRow(tableIOwe, t2Taken, t2Paid, t2Bal);
    }

    private void addHeaderRow(TableLayout table) {
        TableRow row = new TableRow(this);
        row.addView(createTextView("Name", true));
        row.addView(createTextView("Taken", true));
        row.addView(createTextView("Paid", true));
        row.addView(createTextView("Balance", true));
        table.addView(row);
    }

    private void addDataRow(TableLayout table, String name, double taken, double paid, double bal) {
        TableRow row = new TableRow(this);
        row.addView(createTextView(name, false));
        row.addView(createTextView("$" + String.format("%.2f", taken), false));
        row.addView(createTextView("$" + String.format("%.2f", paid), false));
        row.addView(createTextView("$" + String.format("%.2f", bal), true));
        table.addView(row);
    }

    private void addTotalRow(TableLayout table, double taken, double paid, double bal) {
        TableRow row = new TableRow(this);
        row.addView(createTextView("TOTAL", true));
        row.addView(createTextView("$" + String.format("%.2f", taken), true));
        row.addView(createTextView("$" + String.format("%.2f", paid), true));
        row.addView(createTextView("$" + String.format("%.2f", bal), true));
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));
        table.addView(row);
    }

    private TextView createTextView(String text, boolean isBold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 12, 8, 12);
        tv.setTextColor(Color.BLACK);
        if (isBold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }
}
