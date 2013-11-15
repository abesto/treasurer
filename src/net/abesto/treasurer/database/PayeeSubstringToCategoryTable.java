package net.abesto.treasurer.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.abesto.treasurer.TreasurerContract;

public class PayeeSubstringToCategoryTable implements TreasurerContract.PayeeSubstringToCategory {
    private static final String SQL_CREATE_TABLE = "create table " + TABLE_NAME + "(" +
            _ID + " integer primary key autoincrement, " +
            CATEGORY_ID + " integer, " +
            PAYEE_SUBSTRING + " text, " +
            "foreign key ("+CATEGORY_ID+") references " + TreasurerContract.Category.TABLE_NAME + "(" + TreasurerContract.Category._ID + ")" +
            ")";

    private static final String TAG = "PayeeSubstringToCategoryTable";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int v0, int v1) {
        Log.e(TAG, String.format("unimplemented_upgrade %d %d", v0, v1));
    }

}
