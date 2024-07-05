package ru.code22.mtrade;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class PrefRefundActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsRefundFragment())
				.commit();

	}

	public static class SettingsRefundFragment extends PreferenceFragmentCompat {

		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			addPreferencesFromResource(R.xml.pref_refund);
		}

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
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	

}
