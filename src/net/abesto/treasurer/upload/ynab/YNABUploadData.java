package net.abesto.treasurer.upload.ynab;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.abesto.treasurer.model.FailedToParseSms;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.upload.UploadData;


public class YNABUploadData {
	public String title;
	public List<Transaction> goodTransactions;
	public List<Transaction> noCategoryTransactions;
	public List<FailedToParseSms> failedToParse;
	
	public YNABUploadData() {
		goodTransactions = new LinkedList<Transaction>();
		noCategoryTransactions = new LinkedList<Transaction>();
		failedToParse = new LinkedList<FailedToParseSms>();
	}
	
	public YNABUploadData(List<Transaction> transactions, List<FailedToParseSms> failedToParse) {
		this();
		
		Calendar earliest = null, latest = null;
		
		for (Transaction t : transactions) {
			if (t.hasCategory()) {
                this.goodTransactions.add(t);
			} else {
                this.noCategoryTransactions.add(t);
			}
			if (earliest == null || earliest.compareTo(t.getDate()) > 0) {
				earliest = t.getDate();
			}
			if (latest == null || latest.compareTo(t.getDate()) < 0) {
				latest = t.getDate();
			}
		}
		
		this.title = "Transaction report " + YNABDateFormatter.formatDate(earliest);
		if (earliest != null && !earliest.equals(latest)) {
			this.title += " - " + YNABDateFormatter.formatDate(latest);
		}
		
		this.failedToParse = failedToParse;
	}
	
	public static YNABUploadData fromUploadData(UploadData uploadData) {
		return new YNABUploadData(uploadData.getTransactions(), uploadData.getFailedToParse());
	}
}
