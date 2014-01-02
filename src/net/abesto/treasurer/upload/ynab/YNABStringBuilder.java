package net.abesto.treasurer.upload.ynab;

import net.abesto.treasurer.model.FailedToParseSms;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.upload.UploadData;

import java.util.List;

public class YNABStringBuilder {
	private static final String csvHeader = "Date,Payee,Category,Memo,Outflow,Inflow";
	
	public static String csvRow(Transaction t) {
		StringBuilder s = new StringBuilder();
		s.append(YNABDateFormatter.formatDate(t.getDate().getTime())).append(',');
		s.append('"').append(t.getPayee()).append('"').append(',');
		s.append("\"\",");  // No category
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

	public static String toCsv(UploadData data) {
		List<Transaction> transactions = data.getTransactions().subList(0, data.getTransactions().size());
		return YNABStringBuilder.listToCsv(transactions);
	}
	
	public static String buildReport(UploadData data) {
		StringBuilder sb = new StringBuilder(data.getTitle()).append("\n");
		sb.append(data.getTransactions().size()).append(" transactions successfully parsed\n\n");

		sb.append("Failed to parse ").append(data.getFailedToParse().size()).append(" SMS messages\n");
		for (FailedToParseSms line : data.getFailedToParse()) {
			sb.append(line.getMessage()).append("\n");
		}
		
		return sb.toString();
	}
}
