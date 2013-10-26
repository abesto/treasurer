package net.abesto.treasurer;


import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import net.abesto.treasurer.parsers.ParseResult;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParserStoreAdapter;
import net.abesto.treasurer.upload.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends ListActivity {

	private Store<Transaction> transactionStore;
	private Store<String> failedToParseStore;
	private SmsReceiver receiver;
    private static final String otp = "+36309400700";

    private TransactionAdapter adapter;
    private SmsParserStoreAdapter parser;

    private static final int REQUEST_CODE_LOAD = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        initializeStores();
        UploaderFactory.initializeComponent(this);
        initializeListAdapter();
        initializeParser();
        registerSmsListener();
        registerOnCreateContextMenuHandler();
    }

    private void registerSmsListener() {
        Set<String> wantedSenders= new HashSet<String>();
        wantedSenders.add(otp);
        receiver = new SmsReceiver(wantedSenders, new SmsReceiver.Handler() {
            @Override
            public void handle(String sms) {
                ParseResult r = parser.parse(sms);
                if (r.isSuccess()) {
                    adapter.add(r.getTransaction());
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
    }

    private void initializeParser() {
        parser = new SmsParserStoreAdapter(
                ParserFactory.getInstance().buildFromConfig(),
                transactionStore, failedToParseStore
        );
    }

    private void initializeListAdapter() {
        try {
            adapter = new TransactionAdapter(
                    this, getListView().getId(),
                    new ArrayList<Transaction>(transactionStore.get()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("init", "failed to create adapter");
        }
        setListAdapter(adapter);
    }

    private void initializeStores() {
        StoreFactory.initializeComponent(this);
        StoreFactory storeFactory = StoreFactory.getInstance();
        transactionStore = storeFactory.transactionStore();
        failedToParseStore = storeFactory.failedToParseStore();
    }

    private void registerOnCreateContextMenuHandler() {
        final MenuItem.OnMenuItemClickListener deleteClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                Transaction t = adapter.getItem(info.position);
                try {
                    StoreFactory.getInstance().transactionStore().remove(t);
                    adapter.remove(t);
                } catch (Exception e) {
                    e.printStackTrace();
                    SimpleAlertDialog.show(getApplicationContext(), "Failed to remove transaction", e.toString());
                }
                return true;
            }
        };

        View.OnCreateContextMenuListener createListContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
                Transaction t = adapter.getItem(info.position);
                contextMenu.setHeaderTitle(t.getFlow() + " " + t.getCategory() + " " + t.getDate());
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

    @SuppressWarnings("UnusedDeclaration")
    public void onLoadClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
        Intent intent = new Intent(this, LoadActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOAD);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_LOAD:
                repopulateFromStore();
                break;
            default:
                Log.w("MainActivity.onActivityResult", "Unknown request code " + requestCode);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onClearClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
        try {
            transactionStore.flush();
            failedToParseStore.flush();
            adapter.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    @SuppressWarnings("UnusedDeclaration")
    public void onSendClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
        removeCacheFiles();
        UploadData data;
        try {
            data = UploadData.fromStore(this);
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

    private void repopulateFromStore() {
        adapter.clear();
        try {
            for (Transaction t : transactionStore.get()) {
                adapter.add(t);
            }
        } catch (Exception e) {
            SimpleAlertDialog.show(this, "Reload failed", e.toString());
        }
    }
	
    @SuppressWarnings("UnusedDeclaration")
    public void onReloadClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
        repopulateFromStore();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }
}