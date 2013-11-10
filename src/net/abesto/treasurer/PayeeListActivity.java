package net.abesto.treasurer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;

import java.util.ArrayList;

public class PayeeListActivity extends ListActivity {
    public static final String TAG = "PayeeListActivity";
    public static final String EXTRA_RULE = "rule";

    private ArrayAdapter<String> adapter;
    private PayeeToCategoryFilter.Rule rule;
    private String addNewItemString;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rule = (PayeeToCategoryFilter.Rule) getIntent().getSerializableExtra(EXTRA_RULE);
        addNewItemString = getResources().getString(R.string.payee_list_new);
        setPayeeAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(rule.getPayeeSubstrings())));
        setTitle(String.format("Treasurer > Categories > %s", rule.getCategory()));
        Log.i(TAG, String.format("onCreate %s", rule));
    }

    public void setPayeeAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
        adapter.add(addNewItemString);
        setListAdapter(adapter);
    }

    private Integer getNewPayeeIndex() {
        return rule.getPayeeSubstrings().size();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (id == getNewPayeeIndex()) {
            Log.i(TAG, "new_list_item_clicked");
            showNewPayeeDialog();
        }
    }

    public void addPayee(String newPayee) {
        adapter.remove(addNewItemString);
        adapter.add(newPayee);
        adapter.add(addNewItemString);
        rule.addPayeeSubstring(newPayee);
        try {
            rule.save();
            Log.i(TAG, "add_new_payee_substring_success");
        } catch (Exception e) {
            Log.e(TAG, String.format("add_new_payee_substring_failed %s %s %s", rule.getUuid(), rule.getCategory(), newPayee), e);
            SimpleAlertDialog.show(this, "Failed to add new payee", e.toString());
        }
    }

    private void showNewPayeeDialog() {
        final EditText textField = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(String.format("Add new payee to %s", rule.getCategory()))
                .setView(textField)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPayee = textField.getText().toString();
                        Log.i(TAG, String.format("entered_new_payee_substring %s", newPayee));
                        addPayee(newPayee);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "cancelled_new_payee_substring_dialog");
                    }
                })
                .show();
    }
}