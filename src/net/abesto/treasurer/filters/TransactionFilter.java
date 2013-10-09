package net.abesto.treasurer.filters;

import net.abesto.treasurer.Transaction;

public interface TransactionFilter {
	void filter(Transaction t);
}
