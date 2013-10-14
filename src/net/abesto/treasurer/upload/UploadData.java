package net.abesto.treasurer.upload;

import java.io.IOException;
import java.util.List;

import android.content.Context;

import net.abesto.treasurer.Store;
import net.abesto.treasurer.Transaction;

public class UploadData {
	private List<Transaction> transactions;
	private List<String> failedToParse;
	
	public UploadData(List<Transaction> transactions, List<String> failedToParse) {
		super();
		this.transactions = transactions;
		this.failedToParse = failedToParse;
	}
	public List<Transaction> getTransactions() {
		return transactions;
	}
	public List<String> getFailedToParse() {
		return failedToParse;
	}
	
	public static UploadData fromStore(Context context) throws IOException, ClassNotFoundException {
		return new UploadData(
				new Store<Transaction>(context, Transaction.class).get(),
				new Store<String>(context, String.class).get()
				);
	}
}
