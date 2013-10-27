package net.abesto.treasurer;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

public class CategoryListActivity extends ListActivity {
    private Store<String> categoryStore;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        categoryStore = StoreFactory.getInstance().categoryStore();
	}
}
