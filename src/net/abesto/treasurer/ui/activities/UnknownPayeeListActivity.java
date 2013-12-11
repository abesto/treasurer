package net.abesto.treasurer.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import net.abesto.treasurer.ui.fragments.UnknownPayeeListFragment;

public class UnknownPayeeListActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Treasurer > Unknown payees");
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UnknownPayeeListFragment())
                .commit();
    }
}