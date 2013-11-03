package net.abesto.treasurer;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;

import java.util.regex.Pattern;

public class PayeeListActivity extends ListActivity {
    public static final String TAG = "PayeeListActivity";
    public static final String EXTRA_RULE = "rule";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayeeToCategoryFilter.Rule rule = (PayeeToCategoryFilter.Rule) getIntent().getSerializableExtra(EXTRA_RULE);
        setListAdapter(
                new ArrayAdapter<PayeeToCategoryFilter.RulePattern>(this, android.R.layout.simple_list_item_1, rule.getPayeePatterns())
        );
        Log.i(TAG, String.format("onCreate %s", rule));
    }
}