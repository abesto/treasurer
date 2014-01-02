package net.abesto.treasurer.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.model.*;

import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class ModelInflater {
    private static String getString(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        return c.getString(index);
    }

    private static Integer getInt(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        if (c.isNull(index)) return null;
        return c.getInt(index);
    }

    private static Long getLong(Cursor c, String f) {
        int index = getColumnIndex(c, f);
        if (c.isNull(index)) return null;
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
        if (cls == Transaction.class) return (T) inflateTransaction(c);
        if (cls == FailedToParseSms.class) return (T) inflateFailedToParseSms(c);
        throw new IllegalArgumentException("Don't know how to inflate " + cls.toString());
    }

    private static FailedToParseSms inflateFailedToParseSms(Cursor c) {
        FailedToParseSms s = new FailedToParseSms(getString(c, TreasurerContract.StringSet.STRING));
        s.setId(getLong(c, TreasurerContract.StringSet._ID));
        return s;
    }

    public static ContentValues deflate(Class<? extends Model> cls, Model obj) {
        if (cls == Transaction.class) return deflateTransaction((Transaction) obj);
        if (cls == FailedToParseSms.class) return deflateFailedToParseSms((FailedToParseSms) obj);
        throw new IllegalArgumentException("Don't know how to deflate " + cls.toString());
    }

    private static ContentValues deflateFailedToParseSms(FailedToParseSms obj) {
        ContentValues v = new ContentValues();
        if (obj.getId() != null) v.put(TreasurerContract.StringSet._ID, obj.getId());
        v.put(TreasurerContract.StringSet.SET_ID, TreasurerContract.StringSet.FAILED_TO_PARSE_SMS_SET);
        v.put(TreasurerContract.StringSet.STRING, obj.getMessage());
        return v;
    }

    private static ContentValues deflateTransaction(Transaction obj) {
        ContentValues v = new ContentValues();
        v.put(TreasurerContract.Transaction.DATE, obj.getDate().getTimeInMillis() / 1000);
        v.put(TreasurerContract.Transaction.PAYEE, obj.getPayee());
        v.put(TreasurerContract.Transaction.MEMO, obj.getMemo());
        v.put(TreasurerContract.Transaction.INFLOW, obj.getInflow());
        v.put(TreasurerContract.Transaction.OUTFLOW, obj.getOutflow());
        return v;
    }

    public static <T> List<T> inflateAll(Class<T> cls, Cursor c) {
        List<T> list = new LinkedList<T>();
        while (c.moveToNext()) {
            list.add(inflate(cls, c));
        }
        return list;
    }

    public static Uri getUri(Class cls) {
        if (cls == FailedToParseSms.class) return Provider.STRING_SET_URI;
        if (cls == Transaction.class) return Provider.TRANSACTIONS_URI;
        throw new IllegalArgumentException("Don't know the ContentProvider URI for class " + cls.toString());
    }

    private static Transaction inflateTransaction(Cursor c) {
       Transaction t = new Transaction(
                new GregorianCalendar(),
                getString(c, TreasurerContract.Transaction.PAYEE),
                getString(c, TreasurerContract.Transaction.MEMO),
                getInt(c, TreasurerContract.Transaction.OUTFLOW),
                getInt(c, TreasurerContract.Transaction.INFLOW)
        );
        t.setId(c.getLong(0));
        t.getDate().setTimeInMillis(getLong(c, TreasurerContract.Transaction.DATE) * 1000);
        return t;
    }

    public static <T> T getDefault(Class<T> cls) {
        throw new UnsupportedOperationException();
    }
}
