package net.abesto.treasurer;

import net.abesto.treasurer.filters.PayeeToCategoryFilter;

public class StoreFactory {
    private static StoreFactory instance;

    public Store<Transaction> transactionStore() {
        return Store.getInstance(Transaction.class);
    }

    public Store<String> failedToParseStore() {
        return Store.getInstance("FailedToParse");
    }

    public Store<PayeeToCategoryFilter.Rule> payeeToCategoryRuleStore() {
        return Store.getInstance("PayeeToCategoryRule");
    }

    private StoreFactory() {}

    public static StoreFactory getInstance() {
        if (instance == null) {
            instance = new StoreFactory();
        }
        return instance;
    }
}
