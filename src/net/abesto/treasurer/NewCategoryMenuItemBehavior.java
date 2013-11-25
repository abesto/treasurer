package net.abesto.treasurer;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class NewCategoryMenuItemBehavior {
    Activity activity;

    public NewCategoryMenuItemBehavior(Activity activity) {
        this.activity = activity;
    }

    public Boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.category_list_action_create) {
            new NewCategoryDialog(activity).show();
            return true;
        }
        return null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.category_list, menu);
        return true;
    }
}
