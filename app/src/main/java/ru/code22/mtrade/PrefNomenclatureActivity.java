package ru.code22.mtrade;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class PrefNomenclatureActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsNomenclatureFragment())
				.commit();

			
	}

	public static class SettingsNomenclatureFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			addPreferencesFromResource(R.xml.pref_nomenclature);
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
