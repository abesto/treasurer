package net.abesto.treasurer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SelectableSimpleCursorAdapter extends SimpleCursorAdapter {
    private Integer selectedPosition;
    private final static String TAG = "SelectableSimpleCursorAdapter";

    public SelectableSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ListView l = (ListView) parent;

        if (useCheckedItemCompatibilityFix()) {
            v.setBackgroundColor(Color.WHITE);
            if (l.isItemChecked(position)) {
                v.setBackgroundColor(Color.LTGRAY);
                selectedPosition = position;
                Log.d(TAG, "getView_selected_position compatibility " + selectedPosition);
            }
        }

        return v;
    }

    private boolean useCheckedItemCompatibilityFix() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
    }

    public boolean hasCheckedPosition(ListView categoryList) {
        return getCheckedPosition(categoryList) != null;
    }

    public Integer getCheckedPosition(ListView categoryList) {
        if (useCheckedItemCompatibilityFix()) {
            Log.d(TAG, "getCheckedPosition compatibility " + selectedPosition);
            return selectedPosition;
        } else {
            Integer p = categoryList.getCheckedItemPosition();
            if (p == -1) p = null;
            Log.d(TAG, "getCheckedPosition native " + p);
            return p;
        }
    }
}
