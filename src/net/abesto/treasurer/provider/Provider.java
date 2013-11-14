package net.abesto.treasurer.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import net.abesto.treasurer.database.StringSetTable;
import net.abesto.treasurer.database.TreasurerDatabaseHelper;
import org.apache.commons.lang3.ArrayUtils;

import static net.abesto.treasurer.TreasurerContract.*;

public class Provider extends ContentProvider {
    private TreasurerDatabaseHelper database;

    private static final String AUTHORITY = "net.abesto.treasurer.provider";
    private static final String TRANSACTIONS_PATH = "transactions";
    private static final String CATEGORIES_PATH = "categories";
    private static final String CATEGORIES_GET_OR_CREATE_PATH = "categories/get-or-create";
    private static final String PAYEE_SUBSTRING_TO_CATEGORY_PATH = "payee-substring-to-category-rules";
    private static final String STRING_SET_PATH = "stringset";

    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/");
    public static final Uri TRANSACTIONS_URI = Uri.withAppendedPath(BASE_URI, TRANSACTIONS_PATH);
    public static final Uri CATEGORIES_URI = Uri.withAppendedPath(BASE_URI, CATEGORIES_PATH);
    public static final Uri CATEGORIES_GET_OR_CREATE_URI = Uri.withAppendedPath(BASE_URI, CATEGORIES_GET_OR_CREATE_PATH);
    public static final Uri PAYEE_SUBSTRING_TO_CATEGORY_URI = Uri.withAppendedPath(BASE_URI, PAYEE_SUBSTRING_TO_CATEGORY_PATH);
    public static final Uri STRING_SET_URI = Uri.withAppendedPath(BASE_URI, STRING_SET_PATH);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TRANSACTIONS = 10;
    private static final int TRANSACTION_ID = 11;
    private static final int CATEGORIES = 12;
    private static final int CATEGORY_ID = 13;
    private static final int CATEGORY_GET_OR_CREATE = 14;
    private static final int PAYEE_SUBSTRING_TO_CATEGORY_RULES = 15;
    private static final int PAYEE_SUBSTRING_TO_CATEGORY_RULE_ID = 16;
    private static final int STRING_SETS = 17;
    private static final int STRING_SET_ID = 18;
    static {
        sUriMatcher.addURI(AUTHORITY, TRANSACTIONS_PATH, TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, TRANSACTIONS_PATH + "/#", TRANSACTION_ID);
        sUriMatcher.addURI(AUTHORITY, CATEGORIES_PATH, CATEGORIES);
        sUriMatcher.addURI(AUTHORITY, CATEGORIES_PATH + "/#", CATEGORY_ID);
        sUriMatcher.addURI(AUTHORITY, CATEGORIES_GET_OR_CREATE_PATH + "/*", CATEGORY_GET_OR_CREATE);
        sUriMatcher.addURI(AUTHORITY, PAYEE_SUBSTRING_TO_CATEGORY_PATH, PAYEE_SUBSTRING_TO_CATEGORY_RULES);
        sUriMatcher.addURI(AUTHORITY, PAYEE_SUBSTRING_TO_CATEGORY_PATH + "/#", PAYEE_SUBSTRING_TO_CATEGORY_RULE_ID);
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
            queryBuilder.setTables(Transaction.TABLE_NAME + " LEFT JOIN " + Category.TABLE_NAME +
                    " ON " + Transaction.CATEGORY_ID + "=" + Category.FULL_ID);
            if (ArrayUtils.contains(projection, Transaction.COMPUTED_FLOW)) {
                projection[ArrayUtils.indexOf(projection, Transaction.COMPUTED_FLOW)] =
                        Transaction.INFLOW + "-" + Transaction.OUTFLOW + " as " + Transaction.COMPUTED_FLOW;
            }
            if (uriType == TRANSACTION_ID) {
                queryBuilder.appendWhere(Transaction.FULL_ID + "=" + uri.getLastPathSegment());
            }
        } else if (uriType == CATEGORY_GET_OR_CREATE) {
            return getOrCreateCategoryId(uri.getLastPathSegment(), projection, selection, selectionArgs);
        } else if (uriType == CATEGORIES || uriType == CATEGORY_ID) {
            queryBuilder.setTables(Category.TABLE_NAME);
            if (uriType == CATEGORY_ID) {
                queryBuilder.appendWhere(Category.FULL_ID + "=" + uri.getLastPathSegment());
            }
        } else if (uriType == PAYEE_SUBSTRING_TO_CATEGORY_RULES) {
            queryBuilder.setTables(PayeeSubstringToCategory.TABLE_NAME);
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
        } else if (uriType == CATEGORIES) {
            return insertToSqlite(uri, contentValues, Category.TABLE_NAME);
        } else if (uriType == PAYEE_SUBSTRING_TO_CATEGORY_RULES) {
            return insertToSqlite(uri, contentValues, PayeeSubstringToCategory.TABLE_NAME);
        } else if (uriType == STRING_SETS) {
            return insertToSqlite(uri, contentValues, StringSetTable.TABLE_NAME);
        } else if (uriType == TRANSACTION_ID || uriType == CATEGORY_ID) {
            throw new IllegalArgumentException("URI " + uri + " not valid for insert operation");
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
            rowsUpdated = sqlDB.update(Transaction.TABLE_NAME, contentValues, selection, selectionArgs);
        } else if (uriType == TRANSACTION_ID) {
            if (selection.length() != 0) {
                throw new IllegalArgumentException("Selection must be empty for update on " + uri);
            }
            rowsUpdated = sqlDB.update(Transaction.TABLE_NAME,
                    contentValues,
                    Transaction.FULL_ID + "=" + uri.getLastPathSegment(),
                    null);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public Cursor getOrCreateCategoryId(String name, String[] projection, String selection, String[] selectionArgs) {
        Cursor c = query(CATEGORIES_URI, new String[]{Category._ID}, Category.NAME + "=?", new String[]{name}, null);
        if (c.getCount() == 0) {
            ContentValues v = new ContentValues();
            v.put(Category.NAME, name);
            return query(
                    insert(CATEGORIES_URI, v),
                    projection, selection, selectionArgs, null
            );
        } else {
            return c;
        }
    }
}
