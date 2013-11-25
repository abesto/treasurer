package net.abesto.treasurer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;

public class ConcreteDialogFactory {
    private Context context;

    public ConcreteDialogFactory(Context context) {
        this.context = context;
    }

    public AlertDialog.Builder newCategoryDialog() {
        return new TextInputDialogBuilder(context, "Create new category")
            .setPositiveButton("Create", new TextInputDialogBuilder.OnPositiveClickListener() {
                @Override
                public void onClick(String categoryName) {
                    Object result = Queries.getAppInstance().insert(
                            new Category(categoryName)
                    );
                    Log.i(PayeeListActivity.TAG, String.format("created_category %s %s", categoryName, result));
                }
            })
            .setNegativeButton("Cancel", new TextInputDialogBuilder.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    Log.i(PayeeListActivity.TAG, "cancelled_new_category_dialog");
                }
            });
    }

    public AlertDialog.Builder newPayeeDialog(final Category category) {
        return new TextInputDialogBuilder(context, "New payee")
            .setPositiveButton("Add", new TextInputDialogBuilder.OnPositiveClickListener() {
                @Override
                public void onClick(String newPayee) {
                    Log.i(PayeeListActivity.TAG, String.format("entered_new_payee_substring %s", newPayee));
                    PayeeSubstringToCategory rule = new PayeeSubstringToCategory(newPayee, category.getId());
                    Queries.getAppInstance().insert(rule);
                    Log.i(PayeeListActivity.TAG, "add_new_payee_substring_success");
                    Queries.getAppInstance().reapplyAllFilters();
                }
            })
            .setNegativeButton("Cancel", new TextInputDialogBuilder.OnNegativeClickListener() {
                    @Override
                    public void onClick() {
                        Log.i(PayeeListActivity.TAG, "cancelled_new_payee_substring_dialog");
                    }
                });
            };
    }
