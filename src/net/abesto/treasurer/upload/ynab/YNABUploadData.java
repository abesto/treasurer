package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.model.FailedToParseSms;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.upload.UploadData;

import java.util.LinkedList;
import java.util.List;


public class YNABUploadData {
	public String title;
	public List<Transaction> goodTransactions;
	public List<Transaction> noCategoryTransactions;
	public List<FailedToParseSms> failedToParse;
	
	public YNABUploadData(Context context, UploadData uploadData) {
        goodTransactions = new LinkedList<Transaction>();
        noCategoryTransactions = new LinkedList<Transaction>();
        failedToParse = new LinkedList<FailedToParseSms>();
        for (Transaction t : uploadData.getTransactions()) {
            if (t.hasCategory()) {
                this.goodTransactions.add(t);
            } else {
                this.noCategoryTransactions.add(t);
            }
        }
        this.failedToParse = failedToParse;
        this.title = uploadData.getTitle(context);
	}
}
