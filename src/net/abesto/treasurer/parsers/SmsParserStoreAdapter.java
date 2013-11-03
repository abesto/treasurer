package net.abesto.treasurer.parsers;

import android.util.Log;
import net.abesto.treasurer.Store;
import net.abesto.treasurer.Transaction;

public class SmsParserStoreAdapter implements SmsParser {
    private SmsParser parser;
    private Store<String> failedToParseStore;
    private Store<Transaction> transactionStore;
    public static final String TAG = "SmsParserStoreAdapter";

    public SmsParserStoreAdapter(SmsParser parser, Store<Transaction> transactionStore, Store<String> failedToParseStore) {
        this.parser = parser;
        this.failedToParseStore = failedToParseStore;
        this.transactionStore = transactionStore;
    }

    @Override
    public ParseResult parse(String sms) {
        Log.i(TAG, "parse " + sms);
        ParseResult r = parser.parse(sms);
        if (r.isSuccess()) {
            Log.i(TAG, "parse_success " + r.getTransaction());
            try {
                transactionStore.add(r.getTransaction());
            } catch (Exception e) {
                Log.e(TAG, "transactionStore.add failed", e);
            }
        } else {
            Log.i(TAG, "parse_failed");
            try {
                failedToParseStore.add(sms);
            } catch (Exception e) {
                Log.e(TAG, "failedToParseStore.add failed", e);
            }
        }
        return r;
    }
}
