package net.abesto.treasurer.parsers;

import android.net.Uri;
import android.util.Log;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.FailedToParseSms;

public class SmsParserDatabaseAdapter implements SmsParser {
    private SmsParser parser;
    public static final String TAG = "SmsParserDatabaseAdapter";

    public SmsParserDatabaseAdapter(SmsParser parser) {
        this.parser = parser;
    }

    @Override
    public ParseResult parse(String sms) {
        Log.i(TAG, "parse " + sms);
        ParseResult r = parser.parse(sms);
        if (r.isSuccess()) {
            Log.i(TAG, "parse_success " + r.getTransaction());
            try {
                Uri newTransactionUri = Queries.getAppInstance().insert(r.getTransaction());
                Log.i(TAG, "new_transaction " + newTransactionUri);
            } catch (Exception e) {
                Log.e(TAG, "transactionStore.add failed", e);
            }
        } else {
            try {
                FailedToParseSms failedToParseSms = new FailedToParseSms(sms);
                Queries.getAppInstance().insert(failedToParseSms);
                Log.i(TAG, String.format("parse_failed %s %d", sms, failedToParseSms.getId()));
            } catch (Exception e) {
                Log.e(TAG, "failedToParseStore.add failed", e);
            }
        }
        return r;
    }
}
