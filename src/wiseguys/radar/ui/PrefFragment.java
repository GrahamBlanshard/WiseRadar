package wiseguys.radar.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

import wiseguys.radar.R;

public class PrefFragment extends PreferenceFragment {

    public static final String KEY_PREF_GPS = "gps";
    private SharedPreferences.OnSharedPreferenceChangeListener listener;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.radar_pref);
	}

    @Override
    public void onResume() {
        super.onResume();
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(KEY_PREF_GPS)) {
                    CheckBoxPreference showLoc = (CheckBoxPreference)findPreference("show_location");
                    CheckBoxPreference gps = (CheckBoxPreference)findPreference("gps");
                    showLoc.setEnabled(gps.isEnabled());
                }
            }
        };
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }
}
