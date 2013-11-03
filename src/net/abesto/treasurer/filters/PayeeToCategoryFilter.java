package net.abesto.treasurer.filters;

import android.content.Context;
import android.text.Html;
import android.util.Pair;
import net.abesto.treasurer.R;
import net.abesto.treasurer.Store;
import net.abesto.treasurer.StoreFactory;
import net.abesto.treasurer.Transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import android.util.Log;

public class PayeeToCategoryFilter implements TransactionFilter {
    public static final String TAG = "PayeeToCategoryFilter";

    public static class RulePattern implements Serializable {
        private static final long serialVersionUID = 1L;
        private Pattern pattern;
        private String string;

        public RulePattern(String string) {
            this.string = string;
            this.pattern = Pattern.compile(Pattern.quote(string), Pattern.CASE_INSENSITIVE);
        }

        public Pattern getPattern() {
            return pattern;
        }

        public boolean find(String string) {
            return this.pattern.matcher(string).find();
        }

        @Override
        public String toString() {
            return string;
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }
    }

    public static class Rule implements Serializable {
        private static final long serialVersionUID = 1L;
        private String category;
        private Set<RulePattern> payeePatterns;

        public Rule(String category, String... patterns) {
            this.category = category;
            this.payeePatterns = new HashSet<RulePattern>();
            for (String p : patterns) {
                addPattern(p);
            }
        }

        public Boolean matches(String payee) {
            for (RulePattern p : payeePatterns) {
                if (p.find(payee)) {
                    return true;
                }
            }
            return false;
        }

        public String getCategory() {
            return category;
        }

        public RulePattern[] getPayeePatterns() {
            return payeePatterns.toArray(new RulePattern[payeePatterns.size()]);
        }

        public void addPattern(String pattern) {
            payeePatterns.add(new RulePattern(pattern));
        }

        public void removePattern(String toRemove) {
            for (RulePattern p : payeePatterns) {
                if (p.toString().equals(toRemove)) {
                    payeePatterns.remove(p);
                    return;
                }
            }
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
