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
	
	public String getFlow() {
		if (inflow > 0) return inflow.toString();
		return "-" + outflow.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (inflow == null) {
			if (other.inflow != null)
				return false;
		} else if (!inflow.equals(other.inflow))
			return false;
		if (memo == null) {
			if (other.memo != null)
				return false;
		} else if (!memo.equals(other.memo))
			return false;
		if (outflow == null) {
			if (other.outflow != null)
				return false;
		} else if (!outflow.equals(other.outflow))
			return false;
		if (payee == null) {
			if (other.payee != null)
				return false;
		} else if (!payee.equals(other.payee))
			return false;
		return true;
	}
}
