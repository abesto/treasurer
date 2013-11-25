package net.abesto.treasurer;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.provider.Provider;
import org.apache.commons.lang3.ArrayUtils;

public class CreatePayeeSubstringCategoryRuleActivity extends Activity {
    public static final String EXTRA_INITIAL_PAYEE_SUBSTRING = "initial_substring";

    public static final String TAG = "CreatePayeeSubstringCategoryRuleActivity";

    private SelectableSimpleCursorAdapter adapter;
    private NewCategoryMenuItemBehavior newCategoryMenuItemBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView();
        setInitialText();
        initializeCategoryListAdapter();
        registerCategoryListItemClickedHandler();
        registerCreateButtonClickedHandler();
        newCategoryMenuItemBehavior = new NewCategoryMenuItemBehavior(this);
    }

    private void updateCreateButtonEnabled() {
        updateCreateButtonEnabled(adapter.hasSelectedPosition());
    }

    private void updateCreateButtonEnabled(boolean val) {
        Button btn = (Button) findViewById(R.id.create_payee_substring);
        btn.setEnabled(val);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCreateButtonEnabled();
    }

    private void registerCreateButtonClickedHandler() {
        Button btn = (Button) findViewById(R.id.create_payee_substring);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Queries q = Queries.getAppInstance();
                ListView categoryList = (ListView) findViewById(R.id.categories);
                TextView payeeTextView = getPayeeTextView();
                Integer selectedPosition = adapter.getSelectedPosition();
                if (selectedPosition == null) return;
                Cursor c = (Cursor) categoryList.getItemAtPosition(selectedPosition);
                q.insert(new PayeeSubstringToCategory(payeeTextView.getText().toString(), c.getLong(0)));
                q.reapplyAllFilters();
                finish();
            }
        });
    }

    private void setInitialText() {
        if (!getIntent().hasExtra(EXTRA_INITIAL_PAYEE_SUBSTRING)) return;
        TextView textView = getPayeeTextView();
        textView.setText(getIntent().getStringExtra(EXTRA_INITIAL_PAYEE_SUBSTRING));
    }

    private TextView getPayeeTextView() {
        return (TextView) findViewById(R.id.payee_substring);
    }

    private void registerCategoryListItemClickedHandler() {
        final ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCreateButtonEnabled(true);
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCreateButtonEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateCreateButtonEnabled(false);
            }
        });
    }

    private void inflateView() {
        setContentView(R.layout.activity_create_payee_substring_category_rule);
    }

    private void initializeCategoryListAdapter() {
        try {
            String[] columns =  new String[] { TreasurerContract.Category.NAME };
            Cursor c = getContentResolver().query(
                    Provider.CATEGORIES_URI,
                    ArrayUtils.add(columns, 0, TreasurerContract.Category.FULL_ID),
                    null, null, null);
            adapter = new SelectableSimpleCursorAdapter(
                    this, R.layout.selectable_category_list_item, c, columns,
                    new int[]{android.R.id.text1});
            getListView().setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("init", "failed to create adapter");
        }
    }

    private ListView getListView() {
        return (ListView) findViewById(R.id.categories);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return newCategoryMenuItemBehavior.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean result = newCategoryMenuItemBehavior.onOptionsItemSelected(item);
        if (result != null) return result;
        return super.onOptionsItemSelected(item);
    }
}
