package net.abesto.treasurer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;

public class NewCategoryDialog {
    private Context context;

    public NewCategoryDialog(Context context) {
        this.context = context;
    }

    public void show() {
        final EditText textField = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(String.format("Create new category"))
                .setView(textField)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String categoryName = textField.getText().toString();
                        Object result = Queries.getAppInstance().insert(
                                new Category(categoryName)
                        );
                        Log.i(PayeeListActivity.TAG, String.format("created_category %s %s", categoryName, result));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(PayeeListActivity.TAG, "cancelled_new_category_dialog");
                    }
                })
                .show();
    }
}
