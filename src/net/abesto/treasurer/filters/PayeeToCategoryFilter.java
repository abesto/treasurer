package net.abesto.treasurer.filters;

import net.abesto.treasurer.Transaction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

public class PayeeToCategoryFilter implements TransactionFilter {
	private Map<String, List<String>> getMap() {
		Map<String, List<String>> m = new HashMap<String, List<String>>();
		m.put("Monthly Bills: BKV", new LinkedList<String>(Arrays.asList("bérlet")));
		m.put("Everyday Expenses: Groceries", new LinkedList<String>(Arrays.asList("tesco")));
		m.put("Everyday Expenses: Restaurants, Ordered food", new LinkedList<String>(Arrays.asList(
				"étterem", "etterem")));
		return m;
	}

	@Override
	public void filter(Transaction t) {
		Map<String, List<String>> map = getMap();
		for (String category : map.keySet()) {
			for (String payeeSubstring : map.get(category)) {
				Pattern p = Pattern.compile(Pattern.quote(payeeSubstring), Pattern.CASE_INSENSITIVE);
				if (p.matcher(t.getPayee()).find()) {
					t.setCategory(category);
					return;
				}
			}
		}
		Log.w("PayeeToCategoryFilter", "No category found for payee " + t.getPayee());
	}
}
