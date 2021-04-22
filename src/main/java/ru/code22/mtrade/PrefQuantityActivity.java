package ru.code22.mtrade;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefQuantityActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_quantity);
			
	}

	@Override
	protected void onResume() {
		/*
		if (m_nHideFormatSettings)
		{
			Preference customPref = (Preference) findPreference("data_format");
			getPreferenceScreen().removePreference(customPref);
		}
		*/
		super.onResume();
	}

}
