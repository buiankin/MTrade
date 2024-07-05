package ru.code22.mtrade;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

import ru.code22.mtrade.preferences.DatePreference;
import ru.code22.mtrade.preferences.DateTimePreference;
import ru.code22.mtrade.preferences.TimePreference;
import ru.code22.mtrade.preferences.dialog.DatePreferenceDialog;
import ru.code22.mtrade.preferences.dialog.DateTimePreferenceDialog;
import ru.code22.mtrade.preferences.dialog.TimePreferenceDialog;

public class PrefActivity extends AppCompatActivity{
	
	/*
    <CheckBoxPreference
    android:key="notif"
    android:summary="Enable notifications"
    android:title="Notifications" >
	</CheckBoxPreference>
	*/

	/*
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		//g.checkInitByDataAndSetTheme(this);
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getString("app_theme", "DARK").equals("DARK")){
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.LightAppTheme);
		}
		super.onCreate(savedInstanceState);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

	public static class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

		boolean m_bHideFormatSettings;
		boolean m_bWebService;
		boolean m_bCurrency;

		Preference removedPreferenceWebServerAddress;
		Preference removedPreferenceStartDateForOccupiedPlaces;
		Preference removedPreferenceServerAddress;
		Preference removedPreferenceServerUser;
		Preference removedPreferenceServerPassword;
		Preference removedPreferenceServerDirectory;
		Preference removedPreferenceServerAddressSpare;
		Preference removedPreferenceCurrency;


		@Override
		public void onDisplayPreferenceDialog(@NonNull Preference preference) {
			// check if dialog is already showing
//			if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
//				return;
//			}
			DialogFragment dialogFragment = null;
			if (preference instanceof TimePreference) {
				// If it is, then set the dialog fragment to their respective Dialog classes as shown below
				dialogFragment = TimePreferenceDialog.newInstance(preference.getKey());
			} else if (preference instanceof DatePreference) {
				// Alternatively, you can specify the minimum date and maximum date as well
				//dialogFragment = DatePreferenceDialog.newInstance(preference.getKey(), minDate, maxDate);
				String requestKey = UUID.randomUUID().toString();
//				getParentFragmentManager().setFragmentResultListener(requestKey, this, (requestKey1, result) -> {
//					// Handle for result here, if needed
//				}
//				);
				dialogFragment = DatePreferenceDialog.newInstance(preference.getKey());
//				Bundle args = new Bundle();
//				args.putString("requestKey", requestKey);
//				dialogFragment.setArguments(args);
//				dialogFragment.show(getParentFragmentManager(), getClass().getSimpleName());
			} else if (preference instanceof DateTimePreference) {
				dialogFragment = DateTimePreferenceDialog.newInstance(preference.getKey());
				// You can also specify the minimum and maximum date here
				//dialogFragment = DateTimePreferenceDialog.newInstance(preference.getKey(), minDate, maxDate);
			}
			if (dialogFragment != null) {
				// If it is one of our preferences, show it
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getFragmentManager(), "YOUR TAG HERE");
			} else {
				// Let super handle it
				super.onDisplayPreferenceDialog(preference);
			}

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}


		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			addPreferencesFromResource(R.xml.pref);

			removedPreferenceWebServerAddress=null;
			removedPreferenceStartDateForOccupiedPlaces=null;
			removedPreferenceServerAddress=null;
			removedPreferenceServerUser=null;
			removedPreferenceServerPassword=null;
			removedPreferenceServerDirectory=null;
			removedPreferenceServerAddressSpare=null;
			updatePrefVisibility();

			PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

			Intent intent=getActivity().getIntent();
			//Bundle bundle=intent.getBundleExtra("extrasBundle");
			m_bHideFormatSettings=intent.getBooleanExtra("readOnly", false)||Constants.MY_ISTART;

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

			m_bWebService=prefs.getString("data_format", "DM").equals("PH");

			m_bCurrency=prefs.getString("data_format", "DM").equals("IS")||prefs.getString("data_format", "DM").equals("VK");

			//if (intent.getBooleanExtra("readOnly", false))
			//{
			//	// если существует файл настроек, запретим менять формат
			//	// остальное если и поменяют, то до следуюшего запуска
			//	//findPreference("data_format").setEnabled(false);
			//	findPreference("data_format").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			//		@Override
			//		public boolean onPreferenceClick(Preference preference) {
			//			Toast.makeText(PrefActivity.this, "Data fromat: "+PreferenceManager.getDefaultSharedPreferences(PrefActivity.this).getString("data_format", "Unknown"), Toast.LENGTH_LONG).show();
			//			return true;
			//		}
			//	});
			//	//EditTextPreference passwordText=(EditTextPreference)findPreference("password");
			//}

//			// 24.06.2024 перенес в свойства android:inputType="textNoSuggestions", TODO проверить, что это работает
//			//((EditTextPreference)this.findPreference("server_user")).getEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		}

		void updatePrefVisibility() {
			PreferenceScreen rootPreferences=getPreferenceScreen();
			if (m_bHideFormatSettings) {
				Preference customPref = (Preference) findPreference("data_format");
				if (customPref != null)
					rootPreferences.removePreference(customPref);
			}
			if (m_bWebService) {
				Preference customPref = (Preference) findPreference("server_address");
				if (customPref != null) {
					removedPreferenceServerAddress = customPref;
					rootPreferences.removePreference(customPref);
				}
				customPref = (Preference) findPreference("server_user");
				if (customPref != null) {
					removedPreferenceServerUser = customPref;
					rootPreferences.removePreference(customPref);
				}
				customPref = (Preference) findPreference("server_password");
				if (customPref != null) {
					removedPreferenceServerPassword = customPref;
					rootPreferences.removePreference(customPref);
				}
				customPref = (Preference) findPreference("server_directory");
				if (customPref != null) {
					removedPreferenceServerDirectory = customPref;
					rootPreferences.removePreference(customPref);
				}
				if (removedPreferenceWebServerAddress != null) {
					rootPreferences.addPreference(removedPreferenceWebServerAddress);
					removedPreferenceWebServerAddress = null;
				}
				if (removedPreferenceStartDateForOccupiedPlaces != null) {
					rootPreferences.addPreference(removedPreferenceStartDateForOccupiedPlaces);
					removedPreferenceStartDateForOccupiedPlaces = null;
				}
				customPref = (Preference) findPreference("server_address_spare");
				if (customPref != null) {
					removedPreferenceServerAddressSpare = customPref;
					rootPreferences.removePreference(customPref);
				}

			} else {
				Preference customPref = (Preference) findPreference("web_server_address");
				if (customPref != null) {
					removedPreferenceWebServerAddress = customPref;
					rootPreferences.removePreference(customPref);
				}
				customPref = (Preference) findPreference("start_date_for_occupied_places");
				if (customPref != null) {
					removedPreferenceStartDateForOccupiedPlaces = customPref;
					rootPreferences.removePreference(customPref);
				}
				if (removedPreferenceServerAddress != null) {
					rootPreferences.addPreference(removedPreferenceServerAddress);
					removedPreferenceServerAddress = null;
				}
				if (removedPreferenceServerUser != null) {
					rootPreferences.addPreference(removedPreferenceServerUser);
					removedPreferenceServerUser = null;
				}
				if (removedPreferenceServerPassword != null) {
					rootPreferences.addPreference(removedPreferenceServerPassword);
					removedPreferenceServerPassword = null;
				}
				if (removedPreferenceServerDirectory != null) {
					rootPreferences.addPreference(removedPreferenceServerDirectory);
					removedPreferenceServerDirectory = null;
				}

				if (m_bCurrency) {
					if (removedPreferenceCurrency != null) {
						rootPreferences.addPreference(removedPreferenceCurrency);
						removedPreferenceCurrency = null;
					}
				} else {
					// сделать невидимым
					customPref = (Preference) findPreference("currency");
					if (customPref != null) {
						removedPreferenceCurrency = customPref;
						rootPreferences.removePreference(customPref);
					}
				}
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (key.equals("app_theme")) {
				Activity activity=getActivity();
				// 22.05.2020 добавлена проверка
				if (activity!=null)
					activity.recreate();
			}
			if (key.equals("data_format")) {
				m_bWebService = prefs.getString("data_format", "DM").equals("PH");
                m_bCurrency=prefs.getString("data_format", "DM").equals("IS")||prefs.getString("data_format", "DM").equals("VK");
				updatePrefVisibility();
			}
			if (key.equals("currency"))
            {
                MySingleton g=MySingleton.getInstance();
                g.Common.m_currency=prefs.getString("currency", "DEFAULT");
            }

		}
	}

}
