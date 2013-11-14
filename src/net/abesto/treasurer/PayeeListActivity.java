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
import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;

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
            category = Queries.get(Category.class, categoryId);
        } catch (ObjectNotFoundException e) {
            Log.e(TAG, "no_such_category " + categoryId);
            SimpleAlertDialog.show(this, "Category not found", "Failed to find category");
            finish();
            return;
        }
        setTitle(String.format("Treasurer > Categories > %s", category.getName()));
        Log.i(TAG, String.format("onCreate %s", categoryId));
    }

    public void addPayee(String newPayee) {
        PayeeSubstringToCategory rule = new PayeeSubstringToCategory(newPayee, category.getId());
        Queries.insert(rule);
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
}