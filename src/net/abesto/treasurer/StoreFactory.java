package net.abesto.treasurer;

public class StoreFactory {
    private static StoreFactory instance;

    public Store<Transaction> transactionStore() {
        return Store.getInstance(Transaction.class);
    }

    public Store<String> failedToParseStore() {
        return Store.getInstance("FailedToParse");
    }

    public Store<String> categoryStore() {
        return Store.getInstance("Category");
    }

    private StoreFactory() {}

    public static StoreFactory getInstance() {
        if (instance == null) {
            instance = new StoreFactory();
        }
        return instance;
    }
}
