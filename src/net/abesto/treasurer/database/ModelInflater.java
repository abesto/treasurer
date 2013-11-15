package net.abesto.treasurer.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.model.*;
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
        if (cls == Transaction.class) return (T) inflateTransaction(c);
        if (cls == Category.class) return (T) inflateCategory(c);
        if (cls == PayeeSubstringToCategory.class) return (T) inflatePayeeSubstringToCategory(c);
        throw new IllegalArgumentException("Don't know how to inflate " + cls.toString());
    }

    private static PayeeSubstringToCategory inflatePayeeSubstringToCategory(Cursor c) {
        PayeeSubstringToCategory p = new PayeeSubstringToCategory(
                getString(c, TreasurerContract.PayeeSubstringToCategory.PAYEE_SUBSTRING),
                getLong(c, TreasurerContract.PayeeSubstringToCategory.CATEGORY_ID)
        );
        p.setId(getLong(c, TreasurerContract.PayeeSubstringToCategory._ID));
        return p;
    }

    public static ContentValues deflate(Class<? extends Model> cls, Model obj) {
        if (cls == Transaction.class) return deflateTransaction((Transaction) obj);
        if (cls == PayeeSubstringToCategory.class) return deflatePayeeSubstringToCategory((PayeeSubstringToCategory) obj);
        throw new IllegalArgumentException("Don't know how to deflate " + cls.toString());
    }

    private static ContentValues deflatePayeeSubstringToCategory(PayeeSubstringToCategory obj) {
        ContentValues v = new ContentValues();
        v.put(TreasurerContract.PayeeSubstringToCategory.PAYEE_SUBSTRING, obj.getSubstring());
        v.put(TreasurerContract.PayeeSubstringToCategory.CATEGORY_ID, obj.getCategoryId());
        return v;
    }

    private static ContentValues deflateTransaction(Transaction obj) {
        ContentValues v = new ContentValues();
        v.put(TreasurerContract.Transaction.DATE, obj.getDate().getTime());
        v.put(TreasurerContract.Transaction.PAYEE, obj.getPayee());
        v.put(TreasurerContract.Transaction.CATEGORY_ID, obj.getCategoryId());
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
        if (cls == Category.class) return Provider.CATEGORIES_URI;
        if (cls == FailedToParseSms.class) return Provider.STRING_SET_URI;
        if (cls == PayeeSubstringToCategory.class) return Provider.PAYEE_SUBSTRING_TO_CATEGORY_URI;
        if (cls == Transaction.class) return Provider.TRANSACTIONS_URI;
        throw new IllegalArgumentException("Don't know the ContentProvider URI for class " + cls.toString());
    }

    private static Transaction inflateTransaction(Cursor c) {
       Transaction t = new Transaction(
                new Date(),  // TODO
                getString(c, TreasurerContract.Transaction.PAYEE),
                getLong(c, TreasurerContract.Transaction.CATEGORY_ID),
                getString(c, TreasurerContract.Transaction.MEMO),
                getInt(c, TreasurerContract.Transaction.OUTFLOW),
                getInt(c, TreasurerContract.Transaction.INFLOW)
        );
        t.setId(getLong(c, TreasurerContract.Transaction._ID));
        return t;
    }

    private static Category inflateCategory(Cursor c) {
        Category cat = new Category(
                getString(c, TreasurerContract.Category.NAME)
        );
        cat.setId(getLong(c, TreasurerContract.Category._ID));
        return cat;
    }

    public static <T> T getDefault(Class<T> cls) {
        throw new UnsupportedOperationException();
    }
}