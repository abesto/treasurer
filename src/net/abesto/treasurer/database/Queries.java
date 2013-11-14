package net.abesto.treasurer.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import net.abesto.treasurer.model.Model;

import java.util.*;

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
        return list(cls, null, null);
    }
    public <T> List<T> list(Class<T> cls, String selection)  {
        return list(cls, selection, null);
    }
    public <T> List<T> list(Class<T> cls, String selection, String[] selectionArgs) {
        Cursor c = context.getContentResolver().query(ModelInflater.getUri(cls), null, selection, selectionArgs, null);
        return ModelInflater.inflateAll(cls, c);
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

    public <T> int delete(Class<T> cls, Long id) {
        return context.getContentResolver().delete(
                Uri.withAppendedPath(ModelInflater.getUri(cls), id.toString()),
                null, null
        );
    }
}
