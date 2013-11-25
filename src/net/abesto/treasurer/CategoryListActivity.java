package net.abesto.treasurer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.abesto.treasurer.database.ModelInflater;
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.provider.Provider;
import org.apache.commons.lang3.ArrayUtils;

public class CategoryListActivity extends ListActivity {
    public static final String TAG = "CategoryListActivity";

    private int requestedAction;
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_FOR_SUBSTRING = "substring";

    public static final int ACTION_EDIT_RULES = 1;
    public static final int ACTION_CHOOSE_CATEGORY_ID = 2;

    private NewCategoryMenuItemBehavior newCategoryMenuItemBehavior;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestedAction = getIntent().getIntExtra(EXTRA_ACTION, ACTION_EDIT_RULES);
        if (requestedAction == ACTION_EDIT_RULES) {
            setTitle("Treasurer > Categories");
        } else if (requestedAction == ACTION_CHOOSE_CATEGORY_ID) {
            setTitle(String.format("Select category for %s", getIntent().getStringExtra(EXTRA_CATEGORY_FOR_SUBSTRING)));
        }
        registerOnCreateContextMenuHandler();
        newCategoryMenuItemBehavior = new NewCategoryMenuItemBehavior(this);
        Log.i(TAG, "onCreate " + requestedAction);
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
        if (requestedAction == ACTION_CHOOSE_CATEGORY_ID) {
            returnSelectedCategory(category);
        } else {
            editPayeeSubstringRules(category);
        }
    }

    private void editPayeeSubstringRules(Category category) {
        Log.i(TAG, String.format("clicked_category %d %s", category.getId(), category.getName()));
        Intent intent = new Intent(this, PayeeListActivity.class);
        intent.putExtra(PayeeListActivity.EXTRA_CATEGORY_ID, category.getId());
        startActivity(intent);
    }

    private void returnSelectedCategory(Category category) {
        Intent output = new Intent();
        output.putExtra(EXTRA_CATEGORY_ID, category.getId());
        setResult(RESULT_OK, output);
        finish();
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

    private void registerOnCreateContextMenuHandler() {
        final CategoryListActivity context = this;

        final MenuItem.OnMenuItemClickListener deleteClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                int deletedRows = Queries.getAppInstance().delete(Category.class, info.id);
                if (deletedRows != 1) {
                    Log.e(TAG, String.format("deleted_rows_not_1 %s %s %s", info.position, info.id, deletedRows));
                }
                Queries.getAppInstance().reapplyAllFilters();
                return true;
            }
        };

        final MenuItem.OnMenuItemClickListener renameClicked = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                final Category c;
                try {
                    c = Queries.getAppInstance().get(Category.class, info.id);
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "longclicked_category_not_found_in_db", e);
                    return false;
                }
                new TextInputDialogBuilder(context, "Rename category")
                        .setPositiveButton("Save", new TextInputDialogBuilder.OnPositiveClickListener() {
                            @Override
                            public void onClick(String newName) {
                                String oldName = c.getName();
                                c.setName(newName);
                                Queries.getAppInstance().update(c);
                                Log.i(PayeeListActivity.TAG, String.format("renamed_category %d %s %s",
                                        c.getId(), oldName, c.getName()));
                            }
                        })
                        .setNegativeButton("Cancel", new TextInputDialogBuilder.OnNegativeClickListener() {
                            @Override
                            public void onClick() {
                                Log.i(PayeeListActivity.TAG, "cancelled_rename_category_dialog");
                            }
                        })
                        .show();
                return true;
            }
        };

        View.OnCreateContextMenuListener createListContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
                Category c;
                try {
                    c = Queries.getAppInstance().get(Category.class, info.id);
                } catch (ObjectNotFoundException e) {
                    Log.e(TAG, "longclicked_category_not_found_in_db", e);
                    return;
                }

                contextMenu.setHeaderTitle(c.getName());

                contextMenu.add(Menu.NONE, 0, 0, "Rename").setOnMenuItemClickListener(renameClicked);
                contextMenu.add(Menu.NONE, 1, 1, "Delete").setOnMenuItemClickListener(deleteClicked);
                contextMenu.add(Menu.NONE, 2, 2, "Cancel");
            }
        };

        getListView().setOnCreateContextMenuListener(createListContextMenu);
    }
}
