package net.abesto.treasurer.ui.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.ui.activities.CreatePayeeSubstringCategoryRuleActivity;
import net.abesto.treasurer.R;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.database.Provider;

import java.text.DateFormat;

public class TransactionListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "TransactionListFragment";
    private static final int LOADER_ID = 0;
    private static final String[] columns =  new String[] {
            TreasurerContract.Transaction.COMPUTED_FLOW,
            TreasurerContract.Transaction.PAYEE,
            TreasurerContract.Category.NAME,
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
                    new int[] {R.id.flow, R.id.payee, R.id.category}, 0);
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
        final Context context = getActivity();

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

        final MenuItem.OnMenuItemClickListener createRuleClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String initialPayeeSubstring;
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                try {
                    Transaction t = Queries.getAppInstance().get(Transaction.class, info.id);
                    initialPayeeSubstring = t.getPayee();
                    Log.i(TAG, String.format("clicked_add_payee_rule_from_transaction %d %s", info.id, t.getPayee()));
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, String.format("tarnsaction_gone_away %d", info.id), e);
                    initialPayeeSubstring = "";
                }
                Intent intent = new Intent(context, CreatePayeeSubstringCategoryRuleActivity.class);
                intent.putExtra(CreatePayeeSubstringCategoryRuleActivity.EXTRA_INITIAL_PAYEE_SUBSTRING, initialPayeeSubstring);
                startActivity(intent);
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
                        t.getCategoryName()));
                if (t.getCategory() == null) {
                    contextMenu.add(Menu.NONE, 0, 0, "Create category rule based on payee")
                            .setOnMenuItemClickListener(createRuleClicked);
                }
                contextMenu.add(Menu.NONE, 1, 1, "Delete").setOnMenuItemClickListener(deleteClicked);
                contextMenu.add(Menu.NONE, 2, 2, "Cancel");
            }
        };

        getListView().setOnCreateContextMenuListener(createListContextMenu);
    }
}
