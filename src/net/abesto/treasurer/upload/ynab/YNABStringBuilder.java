package net.abesto.treasurer.upload.ynab;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.upload.ynab.YNABUploadData;

public class YNABStringBuilder {
	private static final String csvHeader = "Date,Payee,Category,Memo,Outflow,Inflow";
	
	public static String csvRow(Transaction t) {
		StringBuilder s = new StringBuilder();
		s.append(YNABDateFormatter.formatDate(t.getDate())).append(',');
		s.append('"').append(t.getPayee()).append('"').append(',');
		s.append('"').append(t.getCategory()).append('"').append(',');
		s.append('"').append(t.getMemo()).append('"').append(',');
		s.append(t.getOutflow()).append(',');
		s.append(t.getInflow()).append(',');
		return s.toString();
	}
	
	public static String listToCsv(List<Transaction> ts) {	
		StringBuilder s = new StringBuilder();
		s.append(csvHeader).append("\n");
		for (Transaction t : ts) {
			s.append(csvRow(t)).append("\n");
		}
		return s.toString();
	}

	public static String toCsv(YNABUploadData data) {
		List<Transaction> transactions = data.goodTransactions.subList(0, data.goodTransactions.size());
		transactions.addAll(data.noCategoryTransactions);
		return YNABStringBuilder.listToCsv(transactions);
	}
	
	public static String buildReport(YNABUploadData data) {		
		StringBuilder sb = new StringBuilder(data.title).append("\n");
		sb.append(data.goodTransactions.size()).append(" transactions successfully parsed\n\n");
		
		sb.append(data.noCategoryTransactions.size()).append(" transactions had unknown payees. Payees:\n");
		Set<String> seenPayees = new HashSet<String>();
		for (Transaction t : data.noCategoryTransactions) {
			String payee = t.getPayee();
			if (seenPayees.contains(payee)) continue;
			sb.append(payee).append("\n");
			seenPayees.add(payee);
		}
		sb.append("\n");
		
		sb.append("Failed to parse ").append(data.failedToParse.size()).append(" SMS messages\n");
		for (String line : data.failedToParse) {
			sb.append(line).append("\n");
		}
		
		return sb.toString();
	}
}
