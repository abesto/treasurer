package net.abesto.treasurer.model;

import android.content.ContentValues;
import android.util.Log;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;


public class Transaction extends Model{
	private Date date;
	private String payee;
	private Long categoryId;
	private String memo;
	private Integer outflow;
	private Integer inflow;
	
	public Transaction(Date date, String payee, Long categoryId, String memo,
			Integer outflow, Integer inflow) {
		this.date = date;
		this.payee = payee;
		this.categoryId = categoryId;
		this.memo = memo;
		this.outflow = outflow;
		this.inflow = inflow;
	}

    public ContentValues asContentValues() {
        ContentValues v = new ContentValues();

        v.put(TreasurerContract.Transaction.DATE, date.getTime());
        v.put(TreasurerContract.Transaction.PAYEE, payee);
        v.put(TreasurerContract.Transaction.CATEGORY_ID, categoryId);
        v.put(TreasurerContract.Transaction.MEMO, memo);
        v.put(TreasurerContract.Transaction.INFLOW, inflow);
        v.put(TreasurerContract.Transaction.OUTFLOW, outflow);
        return v;
    }

    public Date getDate() {
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
            return Queries.get(Category.class, categoryId);
        } catch (ObjectNotFoundException e) {
            Log.e("Transaction", "getCategory_failed_to_find_category " + categoryId);
            return null;
        }
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
	
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}	

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
