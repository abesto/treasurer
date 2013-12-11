package net.abesto.treasurer.ui.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.ui.activities.CreatePayeeSubstringCategoryRuleActivity;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.Provider;

public class UnknownPayeeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "UnknownPayeeListFragment";
    private static final int LOADER_ID = 0;
    private static final String[] columns =  new String[] { TreasurerContract.StringSet.STRING, TreasurerContract.StringSet._ID };

    private SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle b) {
        super.onActivityCreated(b);
        try {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            adapter =  new SimpleCursorAdapter(
                    getActivity(), android.R.layout.simple_list_item_1, null,
                    columns, new int[]{android.R.id.text1}, 0);
            setListAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "failed_to_create_adapter", e);
        }
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        String payee = cursor.getString(cursor.getColumnIndex(TreasurerContract.StringSet.STRING));
        Intent intent = new Intent(getActivity(), CreatePayeeSubstringCategoryRuleActivity.class);
        intent.putExtra(CreatePayeeSubstringCategoryRuleActivity.EXTRA_INITIAL_PAYEE_SUBSTRING, payee);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) throw new RuntimeException("Unknown loader id " + id);
        return new CursorLoader(getActivity(),
                Uri.withAppendedPath(Provider.STRING_SET_URI, String.format("%d", TreasurerContract.StringSet.UNKNOWN_PAYEE_SET)),
                columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}

