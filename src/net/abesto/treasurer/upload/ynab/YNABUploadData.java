package net.abesto.treasurer.upload.ynab;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.TransactionStore;

public class YNABUploadData {
	public String title;
	public List<Transaction> goodTransactions;
	public List<Transaction> noCategoryTransactions;
	public List<String> failedToParse;
	
	public YNABUploadData() {
		goodTransactions = new LinkedList<Transaction>();
		noCategoryTransactions = new LinkedList<Transaction>();
		failedToParse = new LinkedList<String>();
	}
	
	public static YNABUploadData fromTransactionStoreData(TransactionStore.Data td) {
		YNABUploadData yd = new YNABUploadData();
		Date earliest = null, latest = null;
		
		for (Transaction t : td.transactions) {
			if (t.getCategory().length() == 0) {
				yd.noCategoryTransactions.add(t);
			} else {
				yd.goodTransactions.add(t);
			}
			if (earliest == null || earliest.compareTo(t.getDate()) > 0) {
				earliest = t.getDate();
			}
			if (latest == null || latest.compareTo(t.getDate()) < 0) {
				latest = t.getDate();
			}
		}
		
		yd.title = "Transaction report " + YNABDateFormatter.formatDate(earliest);
		if (!earliest.equals(latest)) {
			yd.title += "- " + YNABDateFormatter.formatDate(latest);
		}
		
		yd.failedToParse = td.failedToParse;
		
		return yd;
	}
}
