package net.abesto.treasurer.upload;

import android.content.Context;
import android.preference.PreferenceManager;
import net.abesto.treasurer.R;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.FailedToParseSms;
import net.abesto.treasurer.model.Transaction;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class UploadData {
	private List<Transaction> transactions;
	private List<FailedToParseSms> failedToParse;
    private Context context;
	
	public UploadData(List<Transaction> transactions, List<FailedToParseSms> failedToParse, Context context) {
		super();
		this.transactions = transactions;
		this.failedToParse = failedToParse;
        this.context = context;
	}
	public List<Transaction> getTransactions() {
		return transactions;
	}
	public List<FailedToParseSms> getFailedToParse() {
		return failedToParse;
	}

    public static UploadData fromProvider(Context context) {
        return new UploadData(
                Queries.getAppInstance().list(Transaction.class),
                Queries.getAppInstance().list(FailedToParseSms.class),
                context
        );
    }

    private String formatCalendar(Calendar c) {
        if (c == null) return "N/A";
        return DateFormat.getDateInstance().format(c.getTime());
    }

    public String getTitle() {
        Calendar earliest = null, latest = null;

        for (Transaction t : getTransactions()) {
            if (earliest == null || earliest.compareTo(t.getDate()) > 0) {
                earliest = t.getDate();
            }
            if (latest == null || latest.compareTo(t.getDate()) < 0) {
                latest = t.getDate();
            }
        }

        String template = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getResources().getString(R.string.pref_transactionReportTitle_key),
                context.getResources().getString(R.string.pref_transactionReportTitle_default)
        );
        return String.format(template, formatCalendar(earliest), formatCalendar(latest));
    }
}
