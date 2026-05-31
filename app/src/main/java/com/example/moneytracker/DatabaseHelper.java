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
    private static final int DATABASE_VERSION = 2; // Incremented version
    private static final String TABLE_NAME = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Added DATE TEXT
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, PERSON TEXT, AMOUNT REAL, TYPE TEXT, DATE TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addTransaction(String person, double amount, String type, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PERSON", person);
        contentValues.put("AMOUNT", amount);
        contentValues.put("TYPE", type);
        contentValues.put("DATE", date);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "ID=?", new String[]{String.valueOf(id)});
    }

    // Get ALL transactions
    public List<Transaction> getAllTransactions() {
        return queryDatabase("SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC");
    }

    // Get transactions for a SPECIFIC person
    public List<Transaction> getTransactionsByPerson(String personName) {
        return queryDatabase("SELECT * FROM " + TABLE_NAME + " WHERE PERSON = '" + personName + "' ORDER BY ID DESC");
    }

    // Helper method to read the cursor
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
