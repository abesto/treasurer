package net.abesto.treasurer.ui.activities;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import net.abesto.treasurer.DbxAccountManagerFactory;
import net.abesto.treasurer.R;
import net.abesto.treasurer.TransactionListToDropboxSync;
import net.abesto.treasurer.database.Provider;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.ui.SimpleAlertDialog;
import net.abesto.treasurer.ui.fragments.TransactionListFragment;
import net.abesto.treasurer.upload.DataProvider;
import net.abesto.treasurer.upload.UploadAsyncTask;
import net.abesto.treasurer.upload.UploadData;
import net.abesto.treasurer.upload.UploaderFactory;

import java.io.File;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_LOAD = 1;
    private static final int REQUEST_CODE_LINK_DROPBOX = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new TransactionListFragment())
                .commit();
        UploaderFactory.initializeComponent(this);
        Queries.initializeAppInstance(this);
        TransactionListToDropboxSync.initializeComponent(this);
        TransactionListToDropboxSync.register("ynab-transactions.csv");
        Log.i(TAG, "onCreate");
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_LINK_DROPBOX:
                Log.i(TAG, "dropbox_link_done " + resultCode);
                break;
            default:
                Log.w("MainActivity.onActivityResult", "Unknown request code " + requestCode);
        }
    }

    public void clear() {
        Log.d(TAG, "clear_clicked");
        final MainActivity context = this;
        new SimpleAlertDialog(this, "Clear all transactions",
                "This will remove all transactions from the list. There's no undo functionality.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            getContentResolver().delete(Provider.TRANSACTIONS_URI, null, null);
                            Log.i(TAG, "cleared_transaction_list");
                        } catch (Exception e) {
                            Log.e(TAG, "clear_transation_list_failed", e);
                            SimpleAlertDialog.show(context, "Failed to clear transaction list", e.toString());
                        }
                    }
                })
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .show();
    }
	
    public void sendTransactions() {
        removeCacheFiles();
        UploadData data;
        try {
            data = UploadData.fromProvider(this);
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

    private void linkDropboxAccount() {
        DbxAccountManagerFactory.build(getApplicationContext()).startLink(this, REQUEST_CODE_LINK_DROPBOX);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}