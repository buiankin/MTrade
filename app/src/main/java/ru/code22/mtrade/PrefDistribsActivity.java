package ru.code22.mtrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.widget.Toast;

public class PrefDistribsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsDistribsFragment())
				.commit();

			
	}

	public static class SettingsDistribsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			addPreferencesFromResource(R.xml.pref_distribs);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	

}
