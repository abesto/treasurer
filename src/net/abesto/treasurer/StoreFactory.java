package net.abesto.treasurer;

import android.content.Context;

public class StoreFactory {
    private static StoreFactory instance;
    private static Context context;
    private Store<Transaction> transactionStore;
    private Store<String> failedToParseStore;

    public Store<Transaction> transactionStore() {
        if (transactionStore == null) {
            transactionStore = new Store<Transaction>(context, Transaction.class);
        }
        return transactionStore;
    }

    public Store<String> failedToParseStore() {
        if (failedToParseStore == null) {
            failedToParseStore = new Store<String>(context, String.class);
        }
        return failedToParseStore;
    }

    private StoreFactory() {}

    public static void initializeComponent(Context _context) {
        context = _context;
    }

    public static StoreFactory getInstance() {
        if (instance == null) {
            instance = new StoreFactory();
        }
        return instance;
    }
}
