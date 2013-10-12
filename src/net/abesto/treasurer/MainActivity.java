package net.abesto.treasurer;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.abesto.treasurer.SmsReceiver.Handler;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.filters.TransactionFilter;
import net.abesto.treasurer.parsers.OTPCreditCardUsageParser;
import net.abesto.treasurer.parsers.ParseResult;
import net.abesto.treasurer.parsers.SmsParser;
import net.abesto.treasurer.upload.Mailer;
import net.abesto.treasurer.upload.MailerDataProvider;
import net.abesto.treasurer.upload.PastebinUploader;
import net.abesto.treasurer.upload.PastebinUploaderDataProvider;
import net.abesto.treasurer.upload.UploadAsyncTask;
import net.abesto.treasurer.upload.Uploader;
import net.abesto.treasurer.upload.ynab.YNABMailerDataProvider;
import net.abesto.treasurer.upload.ynab.YNABPastebinUploaderDataProvider;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends Activity {

	private SmsParser parser = new OTPCreditCardUsageParser();
	private TransactionFilter filter = new PayeeToCategoryFilter();
	private TransactionStore store;
	private TransactionAdapter adapter;
	private SmsReceiver receiver;
	protected ProgressDialog progressDialog;
	
	private final String otp = "+36309400700";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressDialog = new ProgressDialog(this);
		
		ListView list = (ListView) findViewById(R.id.transactionList);
		store = new TransactionStore(this);

	    try {
	        adapter = new TransactionAdapter(this, R.id.transactionList, 
	    			new ArrayList<Transaction>(store.get().transactions));
	    	list.setAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("init", "failed to create adapter");
		}
	    
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
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }
	
	public void onLoadClicked(View v) {
		AsyncTask<Void, Integer, List<Transaction>> loadTask = new AsyncTask<Void, Integer, List<Transaction>>(){			
			@Override
			protected void onPreExecute() {
				progressDialog.setTitle("Loading transactions");
				progressDialog.setMessage("Loading transactions from SMS messages in the last 30 days. Please wait...");
				progressDialog.setIndeterminate(false);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setProgress(0);
				progressDialog.show();
			}			
			
			@Override
			protected List<Transaction> doInBackground(Void... params) {
				List<String> messages = loadLastMonthsMessages();
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
			store.flush();
			adapter.clear();
		} catch (Exception e) { 
			e.printStackTrace();
		}			
	}
	
	public void onSendClicked(View v) {
		removeCacheFiles();
		TransactionStore.Data data;
		try {
			data = store.get();
		} catch (Exception e) {
			SimpleAlertDialog.show(this, "Failed to load data", e.toString());
			return;
		}
		
		MailerDataProvider dataProvider = new YNABMailerDataProvider(this, data);
		Uploader uploader = new Mailer(this, dataProvider);
//		PastebinUploaderDataProvider dataProvider = new YNABPastebinUploaderDataProvider(data);
//		Uploader uploader = new PastebinUploader(dataProvider);
		
		UploadAsyncTask uploadTask = new UploadAsyncTask(this, uploader);
		uploadTask.execute();
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
				store.add(t);
				return t;
			} catch (Exception e) {
				e.printStackTrace();
			}
    	} else {
    		try {
				store.failed(sms);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}		
    	return null;
	}
	
	private List<String> loadLastMonthsMessages() {
        ArrayList<String> messages = new ArrayList<String>();

    	Calendar monthAgo = Calendar.getInstance();
    	monthAgo.add(Calendar.MONTH, -1);
        
        final String[] projection =
                new String[] { "body" };
        String selection = "address = ? AND date > ?";
        String[] selectionArgs = new String[]{otp, Long.valueOf(monthAgo.getTimeInMillis()).toString()};
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