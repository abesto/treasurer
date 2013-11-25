package net.abesto.treasurer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.provider.Provider;
import org.apache.commons.lang3.ArrayUtils;

public class UnknownPayeeListActivity extends ListActivity {
    public static final String TAG = "UnknownPayeeListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Treasurer > Unknown payees");
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] columns =  new String[] { TreasurerContract.StringSet.STRING };
            Cursor c = getContentResolver().query(
                    Uri.withAppendedPath(Provider.STRING_SET_URI, String.format("%d", TreasurerContract.StringSet.UNKNOWN_PAYEE_SET)),
                    ArrayUtils.add(columns, 0, TreasurerContract.StringSet._ID),
                    null, null, null);
            setListAdapter(new SimpleCursorAdapter(
                    this, android.R.layout.simple_list_item_1, c, columns,
                    new int[] {android.R.id.text1}));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failed_to_create_adapter");
        }
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        String payee = cursor.getString(cursor.getColumnIndex(TreasurerContract.StringSet.STRING));
        Intent intent = new Intent(this, CreatePayeeSubstringCategoryRuleActivity.class);
        intent.putExtra(CreatePayeeSubstringCategoryRuleActivity.EXTRA_INITIAL_PAYEE_SUBSTRING, payee);
        startActivity(intent);
    }
}

