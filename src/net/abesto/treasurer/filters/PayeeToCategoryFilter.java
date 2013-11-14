package net.abesto.treasurer.filters;

import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.model.Transaction;

import java.text.Normalizer;

import android.util.Log;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.Queries;

public class PayeeToCategoryFilter implements TransactionFilter {
    public static final String TAG = "PayeeToCategoryFilter";

    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    private Boolean matches(String payee, String substring) {
        return normalize(payee).contains(normalize(substring));
    }

//    public static void loadTestData() {
//        Store<Rule> store = getDefaultStore();
//        try {
//            store.flush();
//            store.add(new Rule("Monthly Bills: BKV", "bérlet", "berlet"));
//            store.add(new Rule("Everyday Expenses: Groceries", "tesco", "dm"));
//            store.add(new Rule("Everyday Expenses: Household Goods", "kika", "media markt"));
//            store.add(new Rule("Everyday Expenses: Restaurants, Ordered food", "etterem", "étterem"));
//            store.add(new Rule("Everyday Expenses: Software", "sony"));
//        } catch (IOException e) {
//            Log.e(TAG, "loadTestData failed", e);
//        } catch (ClassNotFoundException e) {
//            Log.e(TAG, "loadTestData failed", e);
//        }
//	}

	@Override
	public void filter(Transaction t) {
        for (PayeeSubstringToCategory rule : Queries.list(PayeeSubstringToCategory.class)) {
            if (matches(t.getPayee(), rule.getSubstring())) {
                Long categoryId = rule.getCategoryId();
                t.setCategoryId(categoryId);
                Log.i(TAG, String.format("matched \"%s\" %d %s",
                           t.getPayee(), categoryId, Queries.get(Category.class, categoryId).getName()));
                return;
            }
        }
        Log.i(TAG, "no_category_found '" + t.getPayee() + '"');
	}
}
