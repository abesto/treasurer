package net.abesto.treasurer.upload;

import java.util.List;

import net.abesto.treasurer.model.FailedToParseSms;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.database.Queries;

public class UploadData {
	private List<Transaction> transactions;
	private List<FailedToParseSms> failedToParse;
	
	public UploadData(List<Transaction> transactions, List<FailedToParseSms> failedToParse) {
		super();
		this.transactions = transactions;
		this.failedToParse = failedToParse;
	}
	public List<Transaction> getTransactions() {
		return transactions;
	}
	public List<FailedToParseSms> getFailedToParse() {
		return failedToParse;
	}

    public static UploadData fromProvider() {
        return new UploadData(
                Queries.list(Transaction.class),
                Queries.list(FailedToParseSms.class)
        );
    }
}
