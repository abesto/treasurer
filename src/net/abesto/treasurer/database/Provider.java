package net.abesto.treasurer.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

import static net.abesto.treasurer.TreasurerContract.*;

public class Provider extends ContentProvider {
    private TreasurerDatabaseHelper database;

    private static final String AUTHORITY = "net.abesto.treasurer.provider";
    private static final String TRANSACTIONS_PATH = "transactions";
    private static final String STRING_SET_PATH = "stringset";

    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/");
    public static final Uri TRANSACTIONS_URI = Uri.withAppendedPath(BASE_URI, TRANSACTIONS_PATH);
    public static final Uri STRING_SET_URI = Uri.withAppendedPath(BASE_URI, STRING_SET_PATH);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TRANSACTIONS = 10;
    private static final int TRANSACTION_ID = 11;
    private static final int STRING_SETS = 17;
    private static final int STRING_SET_ID = 18;
    static {
        sUriMatcher.addURI(AUTHORITY, TRANSACTIONS_PATH, TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, TRANSACTIONS_PATH + "/#", TRANSACTION_ID);
        sUriMatcher.addURI(AUTHORITY, STRING_SET_PATH, STRING_SETS);
        sUriMatcher.addURI(AUTHORITY, STRING_SET_PATH + "/#", STRING_SET_ID);
    }

    @Override
    public boolean onCreate() {
        database = new TreasurerDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sUriMatcher.match(uri);
        if (uriType == TRANSACTIONS || uriType == TRANSACTION_ID) {
            queryBuilder.setTables(Transaction.TABLE_NAME);
            HashMap<String, String> projectionMap = new HashMap<>();
            if (projection == null) projection = new String[] {
                    Transaction.COMPUTED_FLOW,
                    Transaction.DATE,
                    Transaction.INFLOW,
                    Transaction.MEMO,
                    Transaction.OUTFLOW,
                    Transaction.PAYEE
            };
            for (String field : projection) {
                projectionMap.put(field, field);
            }
            projectionMap.put(Transaction.COMPUTED_FLOW,
                    String.format("%s-%s as %s", Transaction.INFLOW, Transaction.OUTFLOW, Transaction.COMPUTED_FLOW));
            projectionMap.put(Transaction._ID, Transaction.FULL_ID);
            queryBuilder.setProjectionMap(projectionMap);
            if (uriType == TRANSACTION_ID) {
                queryBuilder.appendWhere(Transaction.FULL_ID + "=" + uri.getLastPathSegment());
            }
        } else if (uriType == STRING_SETS || uriType == STRING_SET_ID) {
            queryBuilder.setTables(StringSet.TABLE_NAME);
            if (uriType == STRING_SET_ID) {
                queryBuilder.appendWhere(StringSet.SET_ID + "=" + uri.getLastPathSegment());
            }
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                return "vnd.android.cursor.dir/vnd.net.abesto.treasurer." + TRANSACTIONS_PATH;
            case TRANSACTION_ID:
                return "vnd.android.cursor.item/vnd.net.abesto.treasurer." + TRANSACTIONS_PATH;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sUriMatcher.match(uri);
        if (uriType == TRANSACTIONS) {
            return insertToSqlite(uri, contentValues, Transaction.TABLE_NAME);
        } else if (uriType == STRING_SETS) {
            return insertToSqlite(uri, contentValues, StringSetTable.TABLE_NAME);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private Uri insertToSqlite(Uri uri, ContentValues contentValues, String tableName) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        Long id;
        id = sqlDB.insert(tableName, null, contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, id.toString());
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        if (uriType == TRANSACTION_ID) {
            rowsDeleted = sqlDB.delete(Transaction.TABLE_NAME, Transaction._ID + "=" + uri.getLastPathSegment(), null);
        } else if (uriType == TRANSACTIONS) {
            rowsDeleted = sqlDB.delete(Transaction.TABLE_NAME, selection, selectionArgs);
        } else if (uriType == STRING_SET_ID) {
            rowsDeleted = sqlDB.delete(StringSet.TABLE_NAME, StringSet._ID + "=" + uri.getLastPathSegment(), null);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        if (uriType == TRANSACTIONS) {
            if (selection != null && selection.length() != 0) {
                throw new IllegalArgumentException("Selection must be empty for update on " + uri);
            }
            rowsUpdated = sqlDB.update(Transaction.TABLE_NAME, contentValues, selection, selectionArgs);
        } else if (uriType == TRANSACTION_ID) {
            rowsUpdated = sqlDB.update(Transaction.TABLE_NAME,
                    contentValues,
                    Transaction.FULL_ID + "=" + uri.getLastPathSegment(),
                    null);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(TRANSACTIONS_URI, null);  // Update transaction list showing category names
        return rowsUpdated;
    }
}
