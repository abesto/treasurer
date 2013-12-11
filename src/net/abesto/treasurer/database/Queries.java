package net.abesto.treasurer.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.Model;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.model.UnknownPayee;

import java.util.List;

public class Queries {
    private Context context;
    private static Queries appInstance;

    public Queries(Context context) {
        this.context = context;
    }

    public static void initializeAppInstance(Context context) {
        appInstance = new Queries(context);
    }

    public static Queries getAppInstance() {
        return appInstance;
    }

    public <T> List<T> list(Class<T> cls) {
        return list(ModelInflater.getUri(cls), cls, null, null);
    }
    public <T> List<T> list(Class<T> cls, String selection)  {
        return list(ModelInflater.getUri(cls), cls, selection, null);
    }
    public <T> List<T> list(Uri uri, Class<T> cls, String selection, String[] selectionArgs) {
        Cursor c = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
        return ModelInflater.inflateAll(cls, c);
    }

    public <T> T get(Uri uri, Class<T> cls)  {
        List<T> l = list(uri, cls, null, null);
        if (l.size() == 0) return null;
        return l.get(0);
    }
    public <T> T get(Class<T> cls, Long id) throws ObjectNotFoundException {
        if (id == -1) {
            return ModelInflater.getDefault(cls);
        }
        Cursor c = context.getContentResolver().query(
                Uri.withAppendedPath(ModelInflater.getUri(cls), id.toString()),
                null, null, null, null
        );
        c.moveToFirst();
        if (c.isAfterLast()) {
            throw new ObjectNotFoundException();
        }
        return ModelInflater.inflate(cls, c);
    }
    public Category getOrCreateCategory(String name) {
        return get(Uri.withAppendedPath(Provider.CATEGORIES_GET_OR_CREATE_URI, name), Category.class);
    }

    public <T extends Model> Uri insert(T obj) {
        if (obj.isIdSet()) {
            throw new IllegalArgumentException("Tried to insert object that already has an id");
        }
        Uri uri = context.getContentResolver().insert(
                ModelInflater.getUri(obj.getClass()),
                ModelInflater.deflate(obj.getClass(), obj)
        );
        obj.setId(Long.parseLong(uri.getLastPathSegment()));
        return uri;
    }

    public <T extends Model> void update(T obj) {
        if (!obj.isIdSet()) {
            throw new IllegalArgumentException("Can't update an object that doesn't have an id");
        }
        context.getContentResolver().update(
                Uri.withAppendedPath(ModelInflater.getUri(obj.getClass()), obj.getId().toString()),
                ModelInflater.deflate(obj.getClass(), obj),
                null, null
        );
    }

    public <T> int delete(Class<T> cls, Long id) {
        return context.getContentResolver().delete(
                Uri.withAppendedPath(ModelInflater.getUri(cls), id.toString()),
                null, null
        );
    }

    public <T extends Model> int delete(T obj) {
        return delete(obj.getClass(), obj.getId());
    }

    public void reapplyAllFilters() {
        PayeeToCategoryFilter filter = new PayeeToCategoryFilter();
        for (Transaction t : list(Transaction.class)) {
            filter.filter(t);
            update(t);
        }
        for (UnknownPayee p : list(UnknownPayee.class)) {
            if (filter.isPayeeKnown(p.getPayee())) {
                delete(p);
            }
        }
    }
}
