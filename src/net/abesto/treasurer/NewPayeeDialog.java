package net.abesto.treasurer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;

public class NewPayeeDialog {
    private static final String TAG = "NewPayeeDialog";
    private final Context context;

    public interface PostCreateAction {
        public void execute(String newPayee);
    }

    public static class AddToCategory implements PostCreateAction {
        private Category category;

        public AddToCategory(Category category) {
            this.category = category;
        }

        @Override
        public void execute(String newPayee) {
            PayeeSubstringToCategory rule = new PayeeSubstringToCategory(newPayee, category.getId());
            Queries.getAppInstance().insert(rule);
            Log.i(TAG, "add_new_payee_substring_success");
            Queries.getAppInstance().reapplyAllFilters();
        }
    }

    public NewPayeeDialog(Context context) {
        this.context = context;
    }

    public void show(final PostCreateAction action, String initialValue) {
        final EditText textField = new EditText(context);
        textField.setText(initialValue);
        new AlertDialog.Builder(context)
                .setTitle(String.format("Add new payee"))
                .setView(textField)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPayee = textField.getText().toString();
                        Log.i(PayeeListActivity.TAG, String.format("entered_new_payee_substring %s", newPayee));
                        action.execute(newPayee);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(PayeeListActivity.TAG, "cancelled_new_payee_substring_dialog");
                    }
                })
                .show();
    }

    public void show(PostCreateAction action) {
        show(action, "");
    }
}