package net.abesto.treasurer;

import java.io.Serializable;
import java.util.Date;


public class Transaction implements Serializable {
	private static final long serialVersionUID = 4448529823537276240L;
	private Date date;
	private String payee;
	private String category;
	private String memo;
	private Integer outflow;
	private Integer inflow;
	
	public Transaction(Date date, String payee, String category, String memo,
			Integer outflow, Integer inflow) {
		super();
		this.date = date;
		this.payee = payee;
		this.category = category;
		this.memo = memo;
		this.outflow = outflow;
		this.inflow = inflow;
	}

	public Date getDate() {
		return date;
	}

	public String getPayee() {
		return payee;
	}

	public String getCategory() {
		return category;
	}

	public String getMemo() {
		return memo;
	}

	public Integer getOutflow() {
		return outflow;
	}

	public Integer getInflow() {
		return inflow;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}	
}
