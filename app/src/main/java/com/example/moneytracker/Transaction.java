package com.example.moneytracker;

public class Transaction {
    private int id;
    private String person;
    private double amount;
    private String type; 
    private String date; // NEW: Added Date

    public Transaction(int id, String person, double amount, String type, String date) {
        this.id = id;
        this.person = person;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public int getId() { return id; }
    public String getPerson() { return person; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getDate() { return date; }
}
