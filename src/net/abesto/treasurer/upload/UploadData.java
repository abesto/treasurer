package net.abesto.treasurer.upload;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import android.database.Cursor;
import net.abesto.treasurer.Store;
import net.abesto.treasurer.StoreFactory;
import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.provider.Provider;

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
	
	public static UploadData fromStore() throws IOException, ClassNotFoundException {
		return new UploadData(
                StoreFactory.getInstance().transactionStore().get(),
                StoreFactory.getInstance().failedToParseStore().get()
        );
	}
//
//    public static UploadData fromProvider(Context context) {
//        LinkedList<Transaction> transactions = new LinkedList<Transaction>();
//        Cursor c = context.getContentResolver().query(Provider.TRANSACTIONS_URI, null, null, null, null);
//        c.moveToFirst();
//        while (!c.isAfterLast()) {
//            transactions.add(Transaction.fromCursor(c));
//        }
//    }
}
