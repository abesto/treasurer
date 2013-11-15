package net.abesto.treasurer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.provider.Provider;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

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
                    "category_id=?", new String[] { category.getId().toString() }, null);
            setListAdapter(new SimpleCursorAdapter(
                    this, android.R.layout.simple_list_item_1, c, columns,
                    new int[] {android.R.id.text1}));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failed to create adapter");
        }
        Log.i(TAG, "onResume");
    }

    public void addPayee(String newPayee) {
        PayeeSubstringToCategory rule = new PayeeSubstringToCategory(newPayee, category.getId());
        Queries.getAppInstance().insert(rule);
        Log.i(TAG, "add_new_payee_substring_success");
    }

    private void showNewPayeeDialog() {
        final EditText textField = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(String.format("Add new payee to %s", category.getName()))
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.payee_list_action_create:
                showNewPayeeDialog();
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