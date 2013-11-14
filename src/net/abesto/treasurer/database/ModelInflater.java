package net.abesto.treasurer.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.model.Model;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.provider.Provider;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ModelInflater {
    private static String getString(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        return c.getString(index);
    }

    private static int getInt(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        return c.getInt(index);
    }

    private static long getLong(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        return c.getLong(index);
    }

    private static int getColumnIndex(Cursor c, String f) {
        int index = c.getColumnIndex(f);
        if (index == -1) {
            throw new IllegalArgumentException(String.format(
                    "Can't find column \"%s\". Choices: %s",
                    f, c.getColumnNames()
            ));
        }
        return index;
    }

    public static <T> T inflate(Class<T> cls, Cursor c) {
        if (cls == Transaction.class) {
            return (T) inflateTransaction(c);
        } else {
            throw new IllegalArgumentException("Don't know how to inflate " + c.toString());
        }
    }

    public static ContentValues deflate(Class<? extends Model> cls, Model obj) {
        if (cls == Transaction.class) {
            return deflateTransaction((Transaction) obj);
        } else {
            throw new IllegalArgumentException("Don't know how to deflate " + cls.toString());
        }
    }

    private static ContentValues deflateTransaction(Transaction obj) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static <T> List<T> inflateAll(Class<T> cls, Cursor c) {
        List<T> list = new LinkedList<T>();
        while (c.moveToNext()) {
            list.add(inflate(cls, c));
        }
        return list;
    }

    public static Uri getUri(Class cls) {
        if (cls == Transaction.class) {
            return Provider.TRANSACTIONS_URI;
        } else {
            throw new IllegalArgumentException("Don't know the ContentProvider URI for class " + cls.toString());
        }
    }

    private static Transaction inflateTransaction(Cursor c) {
        return new Transaction(
                new Date(),  // TODO
                getString(c, TreasurerContract.Transaction.PAYEE),
                getLong(c, TreasurerContract.Transaction.CATEGORY_ID),
                getString(c, TreasurerContract.Transaction.MEMO),
                getInt(c, TreasurerContract.Transaction.OUTFLOW),
                getInt(c, TreasurerContract.Transaction.INFLOW)
        );
    }

    public static <T> T getDefault(Class<T> cls) {
        throw new UnsupportedOperationException();
    }
}
