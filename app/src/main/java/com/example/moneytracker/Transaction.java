package com.example.moneytracker;

public class Transaction {
    private int id;
    private String person;
    private double amount;
    private String type; // "owes_me" or "i_owe"

    public Transaction(int id, String person, double amount, String type) {
        this.id = id;
        this.person = person;
        this.amount = amount;
        this.type = type;
    }

    public int getId() { return id; }
    public String getPerson() { return person; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
}
