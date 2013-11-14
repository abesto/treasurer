package net.abesto.treasurer.parsers;

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
                Queries.insert(r.getTransaction());
            } catch (Exception e) {
                Log.e(TAG, "transactionStore.add failed", e);
            }
        } else {
            try {
                FailedToParseSms failedToParseSms = new FailedToParseSms(sms);
                Queries.insert(failedToParseSms);
                Log.i(TAG, String.format("parse_failed %s %d", sms, failedToParseSms.getId()));
            } catch (Exception e) {
                Log.e(TAG, "failedToParseStore.add failed", e);
            }
        }
        return r;
    }
}
