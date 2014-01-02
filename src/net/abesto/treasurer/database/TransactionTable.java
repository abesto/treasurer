package net.abesto.treasurer.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.abesto.treasurer.TreasurerContract;

public class TransactionTable implements TreasurerContract.Transaction {
    private static final String SQL_CREATE_TABLE = "create table " + TABLE_NAME + "(" +
            _ID + " integer primary key autoincrement, " +
            DATE + " date not null, " +
            PAYEE + " integer, " +
            MEMO + " text, " +
            OUTFLOW + " integer not null, " +
            INFLOW + " integer not null " +
            ")";

    private static final String TAG = "TransactionTable";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int v0, int v1) {
        Log.e(TAG, String.format("unimplemented_upgrade %d %d", v0, v1));
    }
}
