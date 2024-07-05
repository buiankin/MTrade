package ru.code22.mtrade;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ru.code22.mtrade.preferences.DatePreference;
import ru.code22.mtrade.preferences.DateTimePreference;
import ru.code22.mtrade.preferences.TimePreference;
import ru.code22.mtrade.preferences.dialog.DatePreferenceDialog;
import ru.code22.mtrade.preferences.dialog.DateTimePreferenceDialog;
import ru.code22.mtrade.preferences.dialog.TimePreferenceDialog;

public class PrefOrderActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsOrderFragment())
				.commit();

	}

	public static class SettingsOrderFragment extends PreferenceFragmentCompat {

		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			addPreferencesFromResource(R.xml.pref_order);
		}

		@Override
		public void onDisplayPreferenceDialog(Preference preference) {
			DialogFragment dialogFragment = null;
			if (preference instanceof DatePreference) {
				// check if dialog is already showing
				if (getParentFragmentManager().findFragmentByTag(preference.getKey()) != null) {
					return;
				}
				dialogFragment = DatePreferenceDialog.newInstance(preference.getKey());
			}
			if (dialogFragment != null) {
				// If it is one of our preferences, show it
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getFragmentManager(), preference.getKey());
			} else {
				super.onDisplayPreferenceDialog(preference);
			}
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
