package net.abesto.treasurer.parsers;

import net.abesto.treasurer.Store;
import net.abesto.treasurer.Transaction;

public class SmsParserStoreAdapter implements SmsParser {
    private SmsParser parser;
    private Store<String> failedToParseStore;
    private Store<Transaction> transactionStore;

    public SmsParserStoreAdapter(SmsParser parser, Store<Transaction> transactionStore, Store<String> failedToParseStore) {
        this.parser = parser;
        this.failedToParseStore = failedToParseStore;
        this.transactionStore = transactionStore;
    }

    @Override
    public ParseResult parse(String sms) {
        ParseResult r = parser.parse(sms);
        if (r.isSuccess()) {
            try {
                transactionStore.add(r.getTransaction());
            } catch (Exception ignored) {
            }
        } else {
            try {
                failedToParseStore.add(sms);
            } catch (Exception ignored) {
            }
        }
        return r;
    }
}
