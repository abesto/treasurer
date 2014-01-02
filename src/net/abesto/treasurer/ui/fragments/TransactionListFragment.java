package net.abesto.treasurer.ui.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import net.abesto.treasurer.R;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Provider;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Transaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TransactionListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "TransactionListFragment";
    private static final int LOADER_ID = 0;
    private static final String[] columns =  new String[] {
            TreasurerContract.Transaction.COMPUTED_FLOW,
            TreasurerContract.Transaction.PAYEE,
            TreasurerContract.Transaction.DATE,
            TreasurerContract.Transaction._ID };

    private SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle b) {
        super.onActivityCreated(b);
        initializeList();
        registerOnCreateContextMenuHandler();
        Log.d(TAG, "onResume");
    }

    private void initializeList() {
        try {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            adapter =  new SimpleCursorAdapter(
                    getActivity(), R.layout.transaction_list_item, null, columns,
                    new int[] {R.id.flow, R.id.payee, R.id.date}, 0){
                private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    super.bindView(view, context, cursor);
                    TextView date = (TextView) view.findViewById(R.id.date);
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(TreasurerContract.Transaction.DATE)) * 1000);
                    date.setText(dateFormat.format(cal.getTime()));
                }
            };
            setListAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "failed_to_create_adapter", e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) throw new RuntimeException("Unknown loader id " + id);
        return new CursorLoader(getActivity(), Provider.TRANSACTIONS_URI, columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void registerOnCreateContextMenuHandler() {
        final MenuItem.OnMenuItemClickListener deleteClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                int deletedRows = Queries.getAppInstance().delete(Transaction.class, info.id);
                if (deletedRows != 1) {
                    Log.e(TAG, String.format("deleted_rows_not_1 %s %s %s", info.position, info.id, deletedRows));
                }
                return true;
            }
        };

        View.OnCreateContextMenuListener createListContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
                Transaction t;
                try {
                    t = Queries.getAppInstance().get(Transaction.class, info.id);
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "longclicked_transaction_not_found_in_db", e);
                    return;
                }


                contextMenu.setHeaderTitle(String.format("%s: %s %s",
                        DateFormat.getDateInstance().format(t.getDate().getTime()),
                        t.getFlow(),
                        t.getPayee()));
                contextMenu.add(Menu.NONE, 1, 1, "Delete").setOnMenuItemClickListener(deleteClicked);
                contextMenu.add(Menu.NONE, 2, 2, "Cancel");
            }
        };

        getListView().setOnCreateContextMenuListener(createListContextMenu);
    }
}
