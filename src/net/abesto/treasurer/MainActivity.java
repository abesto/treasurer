package net.abesto.treasurer;


import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParserDatabaseAdapter;
import net.abesto.treasurer.provider.Provider;
import net.abesto.treasurer.upload.*;
import net.abesto.treasurer.upload.ynab.YNABDateFormatter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ListActivity {
	private SmsReceiver receiver;

    private SimpleCursorAdapter adapter;
    private SmsParserDatabaseAdapter parser;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_LOAD = 1;
    public static final int REQUEST_CODE_PAYEE_RULES = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        UploaderFactory.initializeComponent(this);
        Queries.initializeAppInstance(this);
        initializeListAdapter();
        initializeParser();
        registerOnCreateContextMenuHandler();
        Log.i(TAG, "onCreate");
    }

    private void initializeParser() {
        parser = new SmsParserDatabaseAdapter(
                ParserFactory.getInstance().buildFromConfig()
        );
    }

    private void initializeListAdapter() {
        try {
            String[] columns =  new String[] {
                    TreasurerContract.Transaction.COMPUTED_FLOW,
                    TreasurerContract.Transaction.PAYEE,
                    TreasurerContract.Category.NAME
            };
            Cursor c = getContentResolver().query(
                    Provider.TRANSACTIONS_URI,
                    ArrayUtils.add(columns, 0, TreasurerContract.Transaction.FULL_ID),
                    null, null, null);
            adapter = new SimpleCursorAdapter(
                    this, R.layout.transaction_list_item, c, columns,
                    new int[] {R.id.flow, R.id.payee, R.id.category});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("init", "failed to create adapter");
        }
        setListAdapter(adapter);
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
                        t.getFlow().toString(),
                        t.getCategoryName()));
                contextMenu.add(Menu.NONE, 0, 0, "Delete").setOnMenuItemClickListener(deleteClicked);
                contextMenu.add(Menu.NONE, 1, 1, "Cancel");
            }
        };

        getListView().setOnCreateContextMenuListener(createListContextMenu);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    public void startLoadActivity() {
        Log.i(TAG, "load_clicked");
        Intent intent = new Intent(this, LoadActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOAD);
    }

    public void openCategoryEditor() {
        Log.i(TAG, "edit_payee_rules_clicked");
        Intent intent = new Intent(this, CategoryListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PAYEE_RULES);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_PAYEE_RULES:
                Log.i(TAG, CategoryListActivity.TAG + " finished");
                break;
            default:
                Log.w("MainActivity.onActivityResult", "Unknown request code " + requestCode);
        }
    }

    public void clear() {
        Log.d(TAG, "clear_clicked");
        try {
            getContentResolver().delete(Provider.TRANSACTIONS_URI, null, null);
            Log.i(TAG, "cleared_transaction_list");
        } catch (Exception e) {
            Log.e(TAG, "clear_transation_list_failed", e);
            SimpleAlertDialog.show(this, "Failed to clear transaction list", e.toString());
        }
    }
	
    public void sendTransactions() {
        removeCacheFiles();
        UploadData data;
        try {
            data = UploadData.fromProvider();
        } catch (Exception e) {
            SimpleAlertDialog.show(this, "Failed to load data", e.toString());
            return;
        }

        if (data.getTransactions().size() == 0) {
            Log.i("onSendClicked", "Ignoring send, no transactions");
            return;
        }

        UploadAsyncTask uploadTask = new UploadAsyncTask(this, UploaderFactory.getInstance().build(
                UploaderFactory.UploaderFormat.YNAB, UploaderFactory.UploaderType.MAIL, data
        ));
        uploadTask.execute();
    }

    private void removeCacheFiles() {
        // Some uploaders generate temporary files, but can't remove them for technical reasons.
        // Removing them when the user hits send is a good bet:
        // Earlier files are likely not needed anymore.
        for (File f : getExternalCacheDir().listFiles()) {
            if (!f.delete()) {
                Log.w("removeCacheFiles", "Failed to delete " + f.getName());
            }
        }
    }

    public void reload() {
        PayeeToCategoryFilter.loadTestData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_payee_rules:
                openCategoryEditor();
                return true;
            case R.id.main_action_reload:
                reload();
                return true;
            case R.id.main_action_clear:
                clear();
                return true;
            case R.id.main_action_load:
                startLoadActivity();
                return true;
            case R.id.main_action_send:
                sendTransactions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}