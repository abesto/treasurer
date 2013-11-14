package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;

public class ParseResult {
	private Boolean success;
	private Transaction transaction;
	
	public ParseResult(Boolean success, Transaction transaction) {
		super();
		this.success = success;
		this.transaction = transaction;
	}
	
	public static ParseResult success(Transaction t) {
		return new ParseResult(true, t);
	}
	
	public static ParseResult fail() {
		return new ParseResult(false, null);
	}
	
	public Boolean isSuccess() {
		return success;
	}
	
	public Transaction getTransaction() {
		return transaction;
	}
}
