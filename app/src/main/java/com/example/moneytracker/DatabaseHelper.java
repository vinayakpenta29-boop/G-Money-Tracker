package com.example.moneytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MoneyTracker.db";
    private static final int DATABASE_VERSION = 3; // Upgraded version
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_MEMBERS = "members";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTransactions = "CREATE TABLE " + TABLE_TRANSACTIONS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, PERSON TEXT, AMOUNT REAL, TYPE TEXT, DATE TEXT)";
        String createMembers = "CREATE TABLE " + TABLE_MEMBERS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT UNIQUE)";
        db.execSQL(createTransactions);
        db.execSQL(createMembers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        onCreate(db);
    }

    // --- MEMBER METHODS ---
    public boolean addMember(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        long result = db.insert(TABLE_MEMBERS, null, contentValues);
        return result != -1;
    }

    public List<String> getAllMembers() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT NAME FROM " + TABLE_MEMBERS + " ORDER BY NAME ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- TRANSACTION METHODS ---
    public boolean addTransaction(String person, double amount, String type, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PERSON", person);
        contentValues.put("AMOUNT", amount);
        contentValues.put("TYPE", type);
        contentValues.put("DATE", date);
        long result = db.insert(TABLE_TRANSACTIONS, null, contentValues);
        return result != -1;
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, "ID=?", new String[]{String.valueOf(id)});
    }

    public List<Transaction> getAllTransactions() {
        return queryDatabase("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY ID DESC");
    }

    public List<Transaction> getTransactionsByPerson(String personName) {
        return queryDatabase("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE PERSON = '" + personName + "' ORDER BY ID DESC");
    }

    private List<Transaction> queryDatabase(String query) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String person = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String type = cursor.getString(3);
                String date = cursor.getString(4);
                list.add(new Transaction(id, person, amount, type, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
