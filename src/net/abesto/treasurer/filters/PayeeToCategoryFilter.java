package net.abesto.treasurer.filters;

import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.model.Transaction;

import java.text.Normalizer;

import android.util.Log;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.UnknownPayee;

public class PayeeToCategoryFilter implements TransactionFilter {
    public static final String TAG = "PayeeToCategoryFilter";
    private Queries queries;

    public PayeeToCategoryFilter(Queries queries) {
        this.queries = queries;
    }

    public PayeeToCategoryFilter() {
        this.queries = Queries.getAppInstance();
    }

    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    private Boolean matches(String payee, String substring) {
        return normalize(payee).contains(normalize(substring));
    }

    public static void loadTestData() {
        Queries q = Queries.getAppInstance();
        q.insert(new PayeeSubstringToCategory("etterem", q.getOrCreateCategory("Everyday Expenses: Restaurants, Ordered food").getId()));
        q.insert(new PayeeSubstringToCategory("berlet", q.getOrCreateCategory("Monthly Bills: BKV").getId()));
        q.insert(new PayeeSubstringToCategory("tesco", q.getOrCreateCategory("Everyday Expenses: Groceries").getId()));
        q.insert(new PayeeSubstringToCategory("dm", q.getOrCreateCategory("Everyday Expenses: Groceries").getId()));
	}

    public boolean isPayeeKnown(String payee) {
        for (PayeeSubstringToCategory rule : queries.list(PayeeSubstringToCategory.class)) {
            if (matches(payee, rule.getSubstring())) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void filter(Transaction t) {
        for (PayeeSubstringToCategory rule : queries.list(PayeeSubstringToCategory.class)) {
            if (matches(t.getPayee(), rule.getSubstring())) {
                Long categoryId = rule.getCategoryId();
                t.setCategoryId(categoryId);
                try {
                    Log.i(TAG, String.format("matched \"%s\" %d %s",
                               t.getPayee(), categoryId, queries.get(Category.class, categoryId).getName()));
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "db_probably_inconsistent", e);
                }
                return;
            }
        }
        t.setCategoryId(null);
        queries.insert(new UnknownPayee(t.getPayee()));
        Log.i(TAG, "no_category_found '" + t.getPayee() + '"');
	}
}
