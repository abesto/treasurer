package net.abesto.treasurer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SelectableSimpleCursorAdapter extends SimpleCursorAdapter {
    private Integer selectedPosition;

    public SelectableSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ListView l = (ListView) parent;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            v.setBackgroundColor(Color.WHITE);
            if (l.isItemChecked(position)) {
                v.setBackgroundColor(Color.LTGRAY);
                selectedPosition = position;
            }
        }

        return v;
    }

    public Integer getSelectedPosition() {
        return selectedPosition;
    }
}
