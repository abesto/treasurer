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
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParserDatabaseAdapter;
import net.abesto.treasurer.provider.Provider;
import net.abesto.treasurer.upload.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ListActivity {
	private SmsReceiver receiver;
    private static final String otp = "+36309400700";

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
        registerSmsListener();
        registerOnCreateContextMenuHandler();
        Log.i(TAG, "onCreate");
    }

    private void registerSmsListener() {
        Set<String> wantedSenders= new HashSet<String>();
        wantedSenders.add(otp);
        receiver = new SmsReceiver(wantedSenders, new SmsReceiver.Handler() {
            @Override
            public void handle(String sms) {
                parser.parse(sms);
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
        Log.i(TAG, "registered_sms_listener");
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
                Cursor c = getContentResolver().query(
                        Uri.withAppendedPath(Provider.TRANSACTIONS_URI, new Long(info.id).toString()),
                        new String[]{
                                TreasurerContract.Transaction.FULL_ID,
                                TreasurerContract.Transaction.COMPUTED_FLOW,
                                TreasurerContract.Category.NAME,
                                TreasurerContract.Transaction.DATE
                        }, null, null, null
                );

                if (c.getCount() == 0) {
                    Log.e(TAG, String.format("longclicked_transaction_not_found %s %s", info.position, info.id));
                    return;
                }
                c.moveToFirst();

                contextMenu.setHeaderTitle(c.getString(1) + " " + c.getString(2) + " " +c.getString(3));
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
        ContentValues v;

        v = new ContentValues();
        v.put(TreasurerContract.Category.NAME, UUID.randomUUID().toString());
        Uri category = getContentResolver().insert(Provider.CATEGORIES_URI, v);

        v = new ContentValues();
        v.put(TreasurerContract.Transaction.CATEGORY_ID, category.getLastPathSegment());
        v.put(TreasurerContract.Transaction.DATE, 500);
        v.put(TreasurerContract.Transaction.INFLOW, 0);
        v.put(TreasurerContract.Transaction.OUTFLOW, 999);
        v.put(TreasurerContract.Transaction.MEMO, "foo memo");
        v.put(TreasurerContract.Transaction.PAYEE, 4);
        getContentResolver().insert(Provider.TRANSACTIONS_URI, v);
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