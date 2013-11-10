package net.abesto.treasurer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.filters.PayeeToCategoryFilter.Rule;

import java.io.IOException;

public class CategoryListActivity extends ListActivity {
    private Store<Rule> ruleStore;
    public static final String TAG = "CategoryListActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setTitle("Treasurer > Categories");
        ruleStore = StoreFactory.getInstance().payeeToCategoryRuleStore();
        Log.i(TAG, "onCreate");
	}

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setListAdapter(
                    new ArrayAdapter<Rule>(this, android.R.layout.simple_list_item_1, ruleStore.get())
            );
        } catch (Exception e) {
            Log.e(TAG, "onResume failed_to_load_rules ", e);
            SimpleAlertDialog.show(this, "Failed to load payee-category rules", e.toString());
            finish();
        }
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Rule rule = (Rule) getListView().getItemAtPosition(position);
        Log.i(TAG, String.format("%s clicked", rule));
        Intent intent = new Intent(this, PayeeListActivity.class);
        intent.putExtra(PayeeListActivity.EXTRA_RULE, rule);
        startActivity(intent);
    }
}
