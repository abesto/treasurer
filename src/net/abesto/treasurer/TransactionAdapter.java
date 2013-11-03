package net.abesto.treasurer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;


public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context context;
    private static final String TAG = "TransactionAdapter";

    public TransactionAdapter(Context context, int resourceId, ArrayList<Transaction> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.transaction_list_item, null);
        }

        Transaction item = getItem(position);
        if (item!= null) {
            TextView flowView = (TextView) view.findViewById(R.id.flow);
            if (flowView != null) {
            	if (item.getInflow() != 0) {
            		flowView.setText(item.getInflow().toString());
            	} else {
            		flowView.setText("-" + item.getOutflow());
            	}
            }

            TextView categoryView = (TextView) view.findViewById(R.id.category);
            if (categoryView != null) {
            	if (item.getCategory().length() > 0) {
            		categoryView.setText(item.getCategory());
            	} else {
            		categoryView.setText("Unknown category");
            	}
            }
            
            TextView payeeView = (TextView) view.findViewById(R.id.payee);
            if (payeeView != null) {
                payeeView.setText(item.getPayee());
            }
         }

        return view;
    }

    @Override
    public void add(Transaction object) {
        super.add(object);
        Log.i(TAG, "added " + object);
    }

    @Override
    public void addAll(Collection<? extends Transaction> collection) {
        super.addAll(collection);
        Log.i(TAG, "added_all" + collection);
    }
}
