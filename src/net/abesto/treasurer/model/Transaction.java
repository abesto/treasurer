package net.abesto.treasurer.model;

import android.util.Log;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.GregorianCalendar;


public class Transaction extends Model{
	private GregorianCalendar date;
	private String payee;
	private Long categoryId;
	private String memo;
	private Integer outflow;
	private Integer inflow;
	
	public Transaction(GregorianCalendar date, String payee, Long categoryId, String memo,
			Integer outflow, Integer inflow) {
		this.date = date;
		this.payee = payee;
		this.categoryId = categoryId;
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

	public Long getCategoryId() {
		return categoryId;
	}

    public Boolean hasCategory() {
        return categoryId != null;
    }

    public Category getCategory() {
        if (!hasCategory()) return null;
        try {
            return Queries.getAppInstance().get(Category.class, categoryId);
        } catch (ObjectNotFoundException e) {
            Log.e("Transaction", "getCategory_failed_to_find_category " + categoryId);
            return null;
        }
    }

    public String getCategoryName() {
        Category c = getCategory();
        if (c == null) return "Unknown category";
        return c.getName();
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
	
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}	

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
