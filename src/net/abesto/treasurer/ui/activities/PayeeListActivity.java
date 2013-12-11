package net.abesto.treasurer.ui.activities;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.ui.ConcreteDialogFactory;
import net.abesto.treasurer.R;
import net.abesto.treasurer.ui.SimpleAlertDialog;
import net.abesto.treasurer.TreasurerContract;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.database.Provider;
import org.apache.commons.lang3.ArrayUtils;

public class PayeeListActivity extends ListActivity {
    public static final String TAG = "PayeeListActivity";
    public static final String EXTRA_CATEGORY_ID = "category";
    private Category category;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long categoryId = getIntent().getLongExtra(EXTRA_CATEGORY_ID, -1);
        if (categoryId == -1) {
            throw new IllegalArgumentException("EXTRA_CATEGORY_ID must not be -1");
        }
        try {
            category = Queries.getAppInstance().get(Category.class, categoryId);
        } catch (ObjectNotFoundException e) {
            Log.e(TAG, "no_such_category " + categoryId);
            SimpleAlertDialog.show(this, "Category not found", "Failed to find category");
            finish();
            return;
        }
        setTitle(String.format("Treasurer > Categories > %s", category.getName()));
        registerOnCreateContextMenuHandler();
        Log.i(TAG, String.format("onCreate %s", categoryId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] columns =  new String[] { TreasurerContract.PayeeSubstringToCategory.PAYEE_SUBSTRING };
            Cursor c = getContentResolver().query(
                    Provider.PAYEE_SUBSTRING_TO_CATEGORY_URI,
                    ArrayUtils.add(columns, 0, TreasurerContract.PayeeSubstringToCategory._ID),
                    "category_id=?", new String[] {category.getId().toString() }, null);
            setListAdapter(new SimpleCursorAdapter(
                    this, android.R.layout.simple_list_item_1, c, columns,
                    new int[] {android.R.id.text1}));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failed to create adapter");
        }
        Log.i(TAG, "onResume");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.payee_list_action_create:
                new ConcreteDialogFactory(this).newPayeeDialog(category).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerOnCreateContextMenuHandler() {
        final MenuItem.OnMenuItemClickListener deleteClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                int deletedRows = Queries.getAppInstance().delete(PayeeSubstringToCategory.class, info.id);
                if (deletedRows != 1) {
                    Log.e(TAG, String.format("deleted_rows_not_1 %s %s %s", info.position, info.id, deletedRows));
                }
                Queries.getAppInstance().reapplyAllFilters();
                return true;
            }
        };

        View.OnCreateContextMenuListener createListContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
                PayeeSubstringToCategory r;
                try {
                    r = Queries.getAppInstance().get(PayeeSubstringToCategory.class, info.id);
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "longclicked_rule_not_found_in_db", e);
                    return;
                }

                try {
                    contextMenu.setHeaderTitle(r.getSubstring() + " -> " + Queries.getAppInstance().get(Category.class, r.getCategoryId()));
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "longclicked_rule_has_invalid_category_id", e);
                    contextMenu.setHeaderTitle(r.getSubstring() + " -> (Unknown)");
                }
                contextMenu.add(Menu.NONE, 0, 0, "Delete").setOnMenuItemClickListener(deleteClicked);
                contextMenu.add(Menu.NONE, 1, 1, "Cancel");
            }
        };

        getListView().setOnCreateContextMenuListener(createListContextMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.payee_list, menu);
        return true;
    }
}