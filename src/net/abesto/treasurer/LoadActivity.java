package net.abesto.treasurer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParserStoreAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Handle loading of historical data
 */
public class LoadActivity extends Activity {
    protected ProgressDialog progressDialog;
    private SmsParserStoreAdapter parser;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parser = new SmsParserStoreAdapter(
                ParserFactory.getInstance().buildFromConfig(),
                StoreFactory.getInstance().transactionStore(),
                StoreFactory.getInstance().failedToParseStore()
        );

        setContentView(R.layout.activity_load);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading transactions");
        progressDialog.setMessage("Loading transactions from SMS messages. Please wait...");
    }

    public void onCancelClicked(@SuppressWarnings("UnusedParameters") View v) {
        finish();
    }

    private Calendar getCalendarFromDatePicker(int id) {
        DatePicker picker = (DatePicker) findViewById(id);
        Calendar calendar = Calendar.getInstance();
        calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
        return calendar;
    }

    public void onLoadClicked(@SuppressWarnings("UnusedParameters") View v) {
        AsyncTask<Void, Integer, Void> loadTask = new AsyncTask<Void, Integer, Void>(){
            @Override
            protected void onPreExecute() {
                progressDialog.setIndeterminate(true);
                progressDialog.setProgress(0);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                List<String> messages = loadMessagesBetween(
                        getCalendarFromDatePicker(R.id.date_from),
                        getCalendarFromDatePicker(R.id.date_until)
                );
                progressDialog.setMax(messages.size());
                progressDialog.setIndeterminate(false);
                for (String sms : messages) {
                    parser.parse(sms);
                    publishProgress(1);
                }
                return null;
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
            protected void onPostExecute(Void result) {
                progressDialog.dismiss();
                finish();
            }
        };
        loadTask.execute();
    }

    private List<String> loadMessagesBetween(Calendar from, Calendar to) {
        ArrayList<String> messages = new ArrayList<String>();

        final String[] projection =
                new String[] { "body" };
        String selection = "address = ? AND date > ? AND date < ?";
        String otp = "+36309400700";
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
}