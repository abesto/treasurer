package net.abesto.treasurer;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;

public class MainActivity extends Activity {

	private SmsParser parser = new OTPCreditCardUsageParser();
	private TransactionFilter filter = new PayeeToCategoryFilter();
	private Store<Transaction> transactionStore;
	private Store<String> failedToParseStore;
	private TransactionAdapter adapter;
	private SmsReceiver receiver;
	private ListView transactionList;
	protected ProgressDialog progressDialog;
	
	private final String otp = "+36309400700";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// UI components
		setContentView(R.layout.activity_main);
		progressDialog = new ProgressDialog(this);		
		transactionList = (ListView) findViewById(R.id.transactionList);
		transactionStore = new Store<Transaction>(this, Transaction.class);
		failedToParseStore = new Store<String>(this, String.class);

	    try {
	        adapter = new TransactionAdapter(this, R.id.transactionList, 
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
	
	public void onLoadClicked(View v) {
		Calendar monthAgo = Calendar.getInstance();
		monthAgo.add(Calendar.MONTH, -1);
		Dialog fromPicker = new DatePickerDialog(this, new OnDateSetListener() {			
			@Override
			public void onDateSet(DatePicker view, final int fromYear, final int fromMonthOfYear, final int fromDayOfMonth) {
				Calendar today = Calendar.getInstance();
				Dialog toPicker = new DatePickerDialog(MainActivity.this, new OnDateSetListener() {			
					@Override
					public void onDateSet(DatePicker view, int toYear, int toMonthOfYear, int toDayOfMonth) {
						Calendar from = Calendar.getInstance();
						from.set(fromYear, fromMonthOfYear, fromDayOfMonth);
						Calendar to = Calendar.getInstance();
						to.set(toYear, toMonthOfYear, toDayOfMonth);
						onLoadDateRangeDlgLoadClicked(from, to);
					}
				}, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
				toPicker.setTitle("Load SMS messages up to");
				toPicker.show();
			}
		}, monthAgo.get(Calendar.YEAR), monthAgo.get(Calendar.MONTH), monthAgo.get(Calendar.DAY_OF_MONTH));
		fromPicker.setTitle("Load SMS messages from");
		fromPicker.show();
	}
	
	public void onLoadDateRangeDlgLoadClicked(final Calendar from, final Calendar to) {
		AsyncTask<Calendar, Integer, List<Transaction>> loadTask = new AsyncTask<Calendar, Integer, List<Transaction>>(){			
			@Override
			protected void onPreExecute() {
				progressDialog.setTitle("Loading transactions");
				progressDialog.setMessage("Loading transactions from SMS messages. Please wait...");
				progressDialog.setIndeterminate(false);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setProgress(0);
				progressDialog.show();
			}			
			
			@Override
			protected List<Transaction> doInBackground(Calendar... params) {
				List<String> messages = loadMessagesBetween(from, to);
				List<Transaction> transactions = new LinkedList<Transaction>();
				progressDialog.setMax(messages.size());
				for (String sms : messages) {
					publishProgress(1);
					Transaction t = handleMessage(sms);
					if (t != null) {
						transactions.add(t);
					}
				}
				return transactions;						
			}
			
			@Override
			protected void onProgressUpdate(Integer... progress) {
				if (progressDialog.isShowing()) {
					for (Integer p : progress) {
						progressDialog.incrementProgressBy(p);
					}
				}
			}
			
			@Override
			protected void onPostExecute(List<Transaction> result) {
				progressDialog.dismiss();
				for (Transaction t : result) {
					adapter.add(t);
				}
			}
		};	
		loadTask.execute();
	}
	
	public void onClearClicked(View v) {
		try {
			transactionStore.flush();
			failedToParseStore.flush();
			adapter.clear();
		} catch (Exception e) { 
			e.printStackTrace();
		}			
	}
	
	public void onSendClicked(View v) {
		removeCacheFiles();
		UploadData data;
		try {
			data = UploadData.fromStore(this);
		} catch (Exception e) {
			SimpleAlertDialog.show(this, "Failed to load data", e.toString());
			return;
		}
		
		MailerDataProvider dataProvider = new YNABMailerDataProvider(this, data);
		Mailer uploader = new Mailer(dataProvider);		
//		PastebinUploaderDataProvider dataProvider = new YNABPastebinUploaderDataProvider(this, data);
//		Uploader uploader = new PastebinUploader(dataProvider);
		
		UploadAsyncTask<Mailer> uploadTask = new UploadAsyncTask<Mailer>(this, uploader);
		uploadTask.execute();
	}
	
	public void onReloadClicked(View v) {
		adapter.clear();
		try {
			for (Transaction t : transactionStore.get()) {
				adapter.add(t);
			}
		} catch (Exception e) {
			SimpleAlertDialog.show(this, "Reload failed", e.toString());
		}
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
			f.delete();
		}
	}

	@SuppressWarnings("unused")
	private List<String> getHardcodedMessages() {
		// Used for manual testing
		return Arrays.asList(
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; hülye payee; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"ez meg eleve rossz"
		);
	}
	
	private Transaction handleMessage(String sms) {
    	Log.i("Parsing", sms);
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
	
	private List<String> loadMessagesBetween(Calendar from, Calendar to) {
        ArrayList<String> messages = new ArrayList<String>();
        
        final String[] projection =
                new String[] { "body" };
        String selection = "address = ? AND date > ? AND date < ?";
        String[] selectionArgs = new String[]{otp, 
        		Long.valueOf(from.getTimeInMillis()).toString(),
        		Long.valueOf(to.getTimeInMillis()).toString()};
        final String sortOrder = "date ASC";

        // Create cursor
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursor != null) {
            try {
                int count = cursor.getCount();
                if (count > 0) {
                    while (cursor.moveToNext()) {
                        messages.add(cursor.getString(0));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return messages;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}