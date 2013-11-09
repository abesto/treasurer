package net.abesto.treasurer.filters;

import net.abesto.treasurer.Store;
import net.abesto.treasurer.StoreFactory;
import net.abesto.treasurer.Transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import android.util.Log;

public class PayeeToCategoryFilter implements TransactionFilter {
    public static final String TAG = "PayeeToCategoryFilter";

    public static class Rule implements Serializable {
        private static final long serialVersionUID = 1L;
        private String category;
        private Set<String> payeeSubstrings;

        public Rule(String category, String... patterns) {
            this.category = category;
            this.payeeSubstrings = new HashSet<String>();
            for (String p : patterns) {
                addPayeeSubstring(p);
            }
        }

        public Boolean matches(String payee) {
            for (String p : payeeSubstrings) {
                if (payee.contains(p)) {
                    return true;
                }
            }
            return false;
        }

        public String getCategory() {
            return category;
        }

        public String[] getPayeeSubstrings() {
            return payeeSubstrings.toArray(new String[payeeSubstrings.size()]);
        }

        public void addPayeeSubstring (String pattern) {
            payeeSubstrings.add(pattern);
        }

        public void removePayeeSubstring(String toRemove) {
            payeeSubstrings.remove(toRemove);
        }

        @Override
        public String toString() {
            // For usage in ArrayAdapter<Rule>
            return getCategory();
        }
    }

	public static void loadTestData() {
        Store<Rule> store = StoreFactory.getInstance().payeeToCategoryRuleStore();
        try {
            store.flush();
            store.add(new Rule("Monthly Bills: BKV", "bérlet", "berlet"));
            store.add(new Rule("Everyday Expenses: Groceries", "tesco", "dm"));
            store.add(new Rule("Everyday Expenses: Household Goods", "kika", "media markt"));
            store.add(new Rule("Everyday Expenses: Restaurants, Ordered food", "etterem", "étterem"));
            store.add(new Rule("Everyday Expenses: Software", "sony"));
        } catch (IOException e) {
            Log.e(TAG, "loadTestData failed", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "loadTestData failed", e);
        }
	}

	@Override
	public void filter(Transaction t) {
        try {
            for (Rule rule : StoreFactory.getInstance().payeeToCategoryRuleStore().get()) {
                if (rule.matches(t.getPayee())) {
                    t.setCategory(rule.getCategory());
                    Log.i(TAG, "matched \"" + t.getPayee() + "\" \"" + rule.getCategory() + '"');
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "rule_store_load_failed", e);
        }
        Log.i(TAG, "no_category_found '" + t.getPayee() + '"');
	}
}
