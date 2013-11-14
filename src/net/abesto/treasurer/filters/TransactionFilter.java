package net.abesto.treasurer.filters;

import net.abesto.treasurer.model.Transaction;

public interface TransactionFilter {
	void filter(Transaction t);
}
