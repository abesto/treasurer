package net.abesto.treasurer.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import net.abesto.treasurer.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
