package net.abesto.treasurer.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.GregorianCalendar;


public class Transaction extends Model{
	private GregorianCalendar date;
	private String payee;
	private String memo;
	private Integer outflow;
	private Integer inflow;
	
	public Transaction(GregorianCalendar date, String payee, String memo,
			Integer outflow, Integer inflow) {
		this.date = date;
		this.payee = payee;
		this.memo = memo;
		this.outflow = outflow;
		this.inflow = inflow;
	}

    public GregorianCalendar getDate() {
		return date;
	}

	public String getPayee() {
		return payee;
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

    public Integer getFlow() {
        return inflow - outflow;
    }
	
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
