package net.abesto.treasurer;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import com.dropbox.sync.android.DbxAccountManager;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.provider.Provider;
import net.abesto.treasurer.upload.DataProvider;
import net.abesto.treasurer.upload.UploadAsyncTask;
import net.abesto.treasurer.upload.UploadData;
import net.abesto.treasurer.upload.UploaderFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.text.DateFormat;

public class MainActivity extends ListActivity {
    private SimpleCursorAdapter adapter;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_LOAD = 1;
    private static final int REQUEST_CODE_PAYEE_RULES = 2;
    private static final int REQUEST_CODE_LINK_DROPBOX = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        UploaderFactory.initializeComponent(this);
        Queries.initializeAppInstance(this);
        initializeListAdapter();
        registerOnCreateContextMenuHandler();
        TransactionListToDropboxSync.initializeComponent(this);
        TransactionListToDropboxSync.register("ynab-transactions.csv");
        Log.i(TAG, "onCreate");
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
        final MainActivity context = this;

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
            case REQUEST_CODE_LINK_DROPBOX:
                Log.i(TAG, "dropbox_link_done " + resultCode);
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
            Log.e(TAG, "uplaod_data_from_provider_failed", e);
            return;
        }

        if (data.getTransactions().size() == 0) {
            Log.i("onSendClicked", "Ignoring send, no transactions");
            return;
        }

        try {
            UploadAsyncTask uploadTask = new UploadAsyncTask(this, UploaderFactory.getInstance().buildFromConfig(data));
            uploadTask.execute();
        } catch (DataProvider.InvalidConfigurationException e) {
            SimpleAlertDialog.show(this, "Upload failed", e.getMessage());
            Log.e(TAG, "upload_failed", e);
        }
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

    private void linkDropboxAccount() {
        DbxAccountManagerFactory.build(getApplicationContext()).startLink(this, REQUEST_CODE_LINK_DROPBOX);
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
            case R.id.main_action_link_dropbox:
                linkDropboxAccount();
                return true;
            case R.id.action_unknown_payees:
                startActivity(new Intent(this, UnknownPayeeListActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}