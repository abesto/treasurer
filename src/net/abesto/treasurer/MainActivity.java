package net.abesto.treasurer;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.filters.TransactionFilter;
import net.abesto.treasurer.parsers.OTPCreditCardUsageParser;
import net.abesto.treasurer.parsers.ParseResult;
import net.abesto.treasurer.parsers.SmsParser;
import net.abesto.treasurer.upload.Mailer;
import net.abesto.treasurer.upload.MailerDataProvider;
import net.abesto.treasurer.upload.UploadAsyncTask;
import net.abesto.treasurer.upload.UploadData;
import net.abesto.treasurer.upload.ynab.YNABMailerDataProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

	private SmsParser parser = new OTPCreditCardUsageParser();
	private TransactionFilter filter = new PayeeToCategoryFilter();
	private Store<Transaction> transactionStore;
	private Store<String> failedToParseStore;
	private TransactionAdapter adapter;
	private SmsReceiver receiver;
	private ListView transactionList;
    private static final String otp = "+36309400700";

    private static final int REQUEST_CODE_LOAD = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// UI components
		setContentView(R.layout.activity_main);
		transactionList = (ListView) findViewById(R.id.transactionList);

        StoreFactory.initializeComponent(this);
        StoreFactory storeFactory = StoreFactory.getInstance();
		transactionStore = storeFactory.transactionStore();
		failedToParseStore = storeFactory.failedToParseStore();

	    try {
	        adapter = new TransactionAdapter(
                    this, R.id.transactionList,
                    new ArrayList<Transaction>(transactionStore.get()));
	    	transactionList.setAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("init", "failed to create adapter");
		}
	    
	    // SMS listener
	    Set<String> wantedSenders= new HashSet<String>();
	    wantedSenders.add(otp);
	    receiver = new SmsReceiver(wantedSenders, new SmsReceiver.Handler() {			
			@Override
			public void handle(String sms) {
				adapter.add(handleMessage(sms));
				
			}
		});
	    IntentFilter filter = new IntentFilter();
	    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
	    registerReceiver(receiver, filter);
	    
	    // List item long click
	    registerForContextMenu(transactionList);
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }
	
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

    public void onClearClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
		try {
			transactionStore.flush();
			failedToParseStore.flush();
			adapter.clear();
		} catch (Exception e) { 
			e.printStackTrace();
		}			
	}
	
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
		MailerDataProvider dataProvider = new YNABMailerDataProvider(this, data);
		Mailer uploader = new Mailer(dataProvider);		
//		PastebinUploaderDataProvider dataProvider = new YNABPastebinUploaderDataProvider(this, data);
//		Uploader uploader = new PastebinUploader(dataProvider);
		
		UploadAsyncTask<Mailer> uploadTask = new UploadAsyncTask<Mailer>(this, uploader);
		uploadTask.execute();
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
	
	public void onReloadClicked(@SuppressWarnings("UnusedParameters") MenuItem m) {
        repopulateFromStore();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  if (v == transactionList) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    Transaction t = adapter.getItem(info.position);
	    menu.setHeaderTitle(t.getFlow() + " " + t.getCategory() + " " + t.getDate());
	    menu.add(Menu.NONE, 0, 0, "Delete");
	    menu.add(Menu.NONE, 1, 1, "Cancel");
	  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  if (menuItemIndex == 0) {
		  Transaction t = adapter.getItem(info.position);
		  try {
			transactionStore.remove(t);
			adapter.remove(t);
		} catch (Exception e) {
			e.printStackTrace();
			SimpleAlertDialog.show(this, "Failed to remove transaction", e.toString());
		}
	  }
	  return true;
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
	
	private Transaction handleMessage(String sms) {
    	ParseResult r = parser.parse(sms);
    	if (r.isSuccess()) {
    		Transaction t = r.getTransaction();
    		filter.filter(t);
    		try {
				transactionStore.add(t);
				return t;
			} catch (Exception e) {
				e.printStackTrace();
			}
    	} else {
    		try {
				failedToParseStore.add(sms);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}		
    	return null;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}