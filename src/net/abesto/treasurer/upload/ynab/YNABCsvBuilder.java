package net.abesto.treasurer.upload.ynab;

import java.util.List;

import net.abesto.treasurer.Transaction;

public class YNABCsvBuilder {
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

}
