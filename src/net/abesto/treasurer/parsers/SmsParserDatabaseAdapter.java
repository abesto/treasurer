package net.abesto.treasurer.parsers;

import android.content.Context;
import android.util.Log;
import net.abesto.treasurer.Store;
import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.provider.Provider;

public class SmsParserDatabaseAdapter implements SmsParser {
    private SmsParser parser;
    private Context context;
    public static final String TAG = "SmsParserDatabaseAdapter";

    public SmsParserDatabaseAdapter(SmsParser parser, Context context) {
        this.parser = parser;
        this.context = context;
    }

    @Override
    public ParseResult parse(String sms) {
        Log.i(TAG, "parse " + sms);
        ParseResult r = parser.parse(sms);
        if (r.isSuccess()) {
            Log.i(TAG, "parse_success " + r.getTransaction());
            try {
                context.getContentResolver().insert(
                        Provider.TRANSACTIONS_URI,
                        r.getTransaction().asContentValues(context)
                );
            } catch (Exception e) {
                Log.e(TAG, "transactionStore.add failed", e);
            }
        } else {
            Log.i(TAG, "parse_failed");
            try {
//                failedToParseStore.add(sms);
            } catch (Exception e) {
                Log.e(TAG, "failedToParseStore.add failed", e);
            }
        }
        return r;
    }
}
