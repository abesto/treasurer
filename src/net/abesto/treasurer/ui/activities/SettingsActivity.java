package net.abesto.treasurer.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import net.abesto.treasurer.ui.fragments.SettingsFragment;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
