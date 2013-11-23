package net.abesto.treasurer.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.abesto.treasurer.TreasurerContract;

public class StringSetTable implements TreasurerContract.StringSet {
    private static final String SQL_CREATE_TABLE = "create table " + TABLE_NAME + "(" +
            _ID    + " integer primary key, " +
            SET_ID + " integer, " +
            STRING + " text, " +
            "unique(" + SET_ID + ", " + STRING + ") on conflict ignore" +
            ")";

    private static final String TAG = "StringSetTable";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int v0, int v1) {
        Log.e(TAG, String.format("unimplemented_upgrade %d %d", v0, v1));
    }
}
