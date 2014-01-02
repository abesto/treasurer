package net.abesto.treasurer.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import net.abesto.treasurer.R;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParserDatabaseAdapter;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Handle loading of historical data
 */
public class LoadActivity extends Activity {
    protected ProgressDialog progressDialog;
    private SmsParserDatabaseAdapter parser;
    public static final String TAG = "LoadActivity";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parser = new SmsParserDatabaseAdapter(
                ParserFactory.getInstance().buildFromConfig()
        );

        setContentView(R.layout.activity_load);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading transactions");
        progressDialog.setMessage("Loading transactions from SMS messages. Please wait...");
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.MONTH, -1);
        updateDatePickerWithCalendar(R.id.date_from, from);
        Calendar until = Calendar.getInstance();
        updateDatePickerWithCalendar(R.id.date_until, until);
        Log.i(TAG, "onStart " + DateFormatUtils.ISO_DATE_FORMAT.format(from)
                + " to " + DateFormatUtils.ISO_DATE_FORMAT.format(until));
    }

    private Calendar getCalendarFromDatePicker(int id) {
        DatePicker picker = (DatePicker) findViewById(id);
        Calendar calendar = Calendar.getInstance();
        calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
        return calendar;
    }

    private void updateDatePickerWithCalendar(int id, Calendar cal) {
        ((DatePicker)findViewById(id)).updateDate(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    private void onLoadClicked() {
        Log.d(TAG, "load_clicked");
        AsyncTask<Void, Integer, Void> loadTask = new AsyncTask<Void, Integer, Void>(){
            @Override
            protected void onPreExecute() {
                Log.i(TAG, "pre_execute");
                progressDialog.setIndeterminate(true);
                progressDialog.setProgress(0);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                List<Pair<String, GregorianCalendar>> messages = loadMessagesBetween(
                        getCalendarFromDatePicker(R.id.date_from),
                        getCalendarFromDatePicker(R.id.date_until)
                );
                progressDialog.setMax(messages.size());
                progressDialog.setIndeterminate(false);
                for (Pair<String, GregorianCalendar> sms : messages) {
                    parser.parse(sms.first, sms.second);
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
                Log.i(TAG, "post_execute");
            }
        };
        loadTask.execute();
    }

    private List<Pair<String, GregorianCalendar>> loadMessagesBetween(Calendar from, Calendar to) {
        ArrayList<Pair<String, GregorianCalendar>> messages = new ArrayList<Pair<String, GregorianCalendar>>();
        Log.i(TAG, "loading_smses_between " + DateFormatUtils.ISO_DATE_FORMAT.format(from)
                + " " + DateFormatUtils.ISO_DATE_FORMAT.format(to));

        final String[] projection =
                new String[] { "body", "date" };
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
                        GregorianCalendar sent = new GregorianCalendar();
                        sent.setTimeInMillis(cursor.getLong(1));
                        messages.add(new Pair<String, GregorianCalendar>(cursor.getString(0), sent));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        Log.i(TAG, "loaded_sms_count " + messages.size());
        return messages;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.load, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.load_action_load:
                onLoadClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}