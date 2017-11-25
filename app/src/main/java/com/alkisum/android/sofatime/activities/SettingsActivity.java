package com.alkisum.android.sofatime.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.alkisum.android.sofatime.R;
import com.alkisum.android.sofatime.utils.Pref;

import butterknife.ButterKnife;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        getFragmentManager().beginTransaction().replace(
                R.id.settings_frame_content, new SettingsFragment()).commit();
    }

    /**
     * SettingsFragment extending PreferenceFragment.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * EditTextPreference for VLC IP address.
         */
        private EditTextPreference vlcIpAddressPref;

        /**
         * EditTextPreference for VLC port.
         */
        private EditTextPreference vlcPortPref;

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            vlcIpAddressPref = (EditTextPreference) findPreference(
                    Pref.VLC_IP_ADDRESS);
            vlcPortPref = (EditTextPreference) findPreference(Pref.VLC_PORT);

            // Initialize summaries
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            vlcIpAddressPref.setSummary(sharedPref.getString(
                    Pref.VLC_IP_ADDRESS, ""));
            vlcPortPref.setSummary(sharedPref.getString(Pref.VLC_PORT, ""));

            // About
            Preference aboutPref = findPreference(Pref.ABOUT);
            aboutPref.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            startActivity(new Intent(getActivity(),
                                    AboutActivity.class));
                            return false;
                        }
                    });
        }

        @Override
        public final void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onSharedPreferenceChanged(
                final SharedPreferences sharedPreferences, final String key) {
            switch (key) {
                case Pref.VLC_IP_ADDRESS:
                    vlcIpAddressPref.setSummary(sharedPreferences.getString(
                            Pref.VLC_IP_ADDRESS, ""));
                    break;
                case Pref.VLC_PORT:
                    vlcPortPref.setSummary(sharedPreferences.getString(
                            Pref.VLC_PORT, ""));
                    break;
                default:
                    break;
            }
        }
    }
}
