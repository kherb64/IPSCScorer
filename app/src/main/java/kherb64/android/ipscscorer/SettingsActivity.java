package kherb64.android.ipscscorer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by herb on 21.03.15.
 */
public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

    public static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    // Keys for result data intent
    public static final String TARGETS_CHANGED = "targets_changed";

    private boolean mTargetsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.

        // TODO change input method of target numbers to spinners
        // TODO limit minimum value to 0 maximum value to 99
        // special type im xml

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_steel_targets_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_paper_targets_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_shooter_key)));

        // initializing must happen after(!) first binding
        mTargetsChanged = false;
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once to initialize the summary before the value is changed.
     * @param preference identifies the preference
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String pref_key = preference.getKey();
        if (pref_key.equals(getString(R.string.pref_steel_targets_key))
                || pref_key.equals(getString(R.string.pref_paper_targets_key))) {
            Log.d (LOG_TAG, "Target number has changed");
            mTargetsChanged = true;
        }
        preference.setSummary(o.toString());
        return true;
    }

    @Override
    public void finish() {
        Log.d (LOG_TAG, "Finishing, do have targets changed? " + mTargetsChanged);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(TARGETS_CHANGED, mTargetsChanged);
        setResult(RESULT_OK, returnIntent);

        super.finish();
    }
}
