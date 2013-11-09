package net.abesto.treasurer.filters;

import net.abesto.treasurer.Store;
import net.abesto.treasurer.StoreFactory;
import net.abesto.treasurer.Transaction;

import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.*;

import android.util.Log;

public class PayeeToCategoryFilter implements TransactionFilter {
    public static final String TAG = "PayeeToCategoryFilter";
    private Store<Rule> store;

    public static class Rule implements Serializable {
        private static final long serialVersionUID = 1L;
        private String category;
        private Set<String> payeeSubstrings;
        private UUID uuid = UUID.randomUUID();

        public Rule(String category, String... patterns) {
            this.category = category;
            this.payeeSubstrings = new HashSet<String>();
            for (String p : patterns) {
                addPayeeSubstring(p);
            }
        }

        private String normalize(String s) {
            return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
        }

        public Boolean matches(String payee) {
            for (String p : payeeSubstrings) {
                if (normalize(payee).contains(normalize(p))) {
                    return true;
                }
            }
            return false;
        }

        public String getCategory() {
            return category;
        }

        public UUID getUuid() {
            return getUuid();
        }

        public Set<String> getPayeeSubstrings() {
            return payeeSubstrings;
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Rule)) {
                return false;
            }
            return ((Rule)o).uuid == uuid;
        }

        public void save(Store<Rule> store) throws IOException, ClassNotFoundException {
            List<Rule> list = store.get();
            list.set(list.indexOf(this), this);
            store.save(list);
        }

        public void save() throws IOException, ClassNotFoundException {
            save(getDefaultStore());
        }
    }

    public PayeeToCategoryFilter(Store<Rule> store) {
        this.store = store;
    }

    public PayeeToCategoryFilter() {
        this(getDefaultStore());
    }

    private static Store<Rule> getDefaultStore() {
        return StoreFactory.getInstance().payeeToCategoryRuleStore();
    }

    public static void loadTestData() {
        Store<Rule> store = getDefaultStore();
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
            for (Rule rule : store.get()) {
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
