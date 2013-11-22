package net.abesto.treasurer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.abesto.treasurer.TreasurerContract;

public class TreasurerDatabaseHelper extends SQLiteOpenHelper {
    public TreasurerDatabaseHelper(Context context) {
        super(context, TreasurerContract.DATABASE_NAME, null, TreasurerContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        TransactionTable.onCreate(sqLiteDatabase);
        CategoryTable.onCreate(sqLiteDatabase);
        PayeeSubstringToCategoryTable.onCreate(sqLiteDatabase);
        StringSetTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int v0, int v1) {
        TransactionTable.onUpgrade(sqLiteDatabase, v0, v1);
        CategoryTable.onUpgrade(sqLiteDatabase, v0, v1);
        PayeeSubstringToCategoryTable.onUpgrade(sqLiteDatabase, v0, v1);
        StringSetTable.onUpgrade(sqLiteDatabase, v0, v1);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        super.onOpen(sqLiteDatabase);
        if (!sqLiteDatabase.isReadOnly()) {
            sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
