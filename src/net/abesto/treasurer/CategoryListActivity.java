package net.abesto.treasurer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.database.ModelInflater;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.provider.Provider;
import org.apache.commons.lang3.ArrayUtils;

public class CategoryListActivity extends ListActivity {
    public static final String TAG = "CategoryListActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setTitle("Treasurer > Categories");
        Log.i(TAG, "onCreate");
	}

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] columns =  new String[] { TreasurerContract.Category.NAME };
            Cursor c = getContentResolver().query(
                    Provider.CATEGORIES_URI,
                    ArrayUtils.add(columns, 0, TreasurerContract.Category.FULL_ID),
                    null, null, null);
            setListAdapter(new SimpleCursorAdapter(
                    this, android.R.layout.simple_list_item_1, c, columns,
                    new int[] {android.R.id.text1}));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("init", "failed to create adapter");
        }
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        Category category = ModelInflater.inflate(Category.class, cursor);
        Log.i(TAG, String.format("clicked_category %d %s", category.getId(), category.getName()));
        Intent intent = new Intent(this, PayeeListActivity.class);
        intent.putExtra(PayeeListActivity.EXTRA_CATEGORY_ID, category.getId());
        startActivity(intent);
    }
}
