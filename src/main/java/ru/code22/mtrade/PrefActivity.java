package ru.code22.mtrade;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

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

		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		boolean m_bHideFormatSettings;
		boolean m_bWebService;
		boolean m_bCurrency;

		Preference removedPreferenceWebServerAddress;
		Preference removedPreferenceStartDateForOccupiedPlaces;
		Preference removedPreferenceServerAddress;
		Preference removedPreferenceServerUser;
		Preference removedPreferenceServerPassword;
		Preference removedPreferenceServerDirectory;
		Preference removedPreferenceServerAddressForWifiConnection;
		Preference removedPreferenceCurrency;

		String m_wifiDescr;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref);

			removedPreferenceWebServerAddress=null;
			removedPreferenceStartDateForOccupiedPlaces=null;
			removedPreferenceServerAddress=null;
			removedPreferenceServerUser=null;
			removedPreferenceServerPassword=null;
			removedPreferenceServerDirectory=null;
			removedPreferenceServerAddressForWifiConnection=null;

			m_wifiDescr=WifiConnection.getWifiConnection(getActivity());
			updatePrefServerWhenWifiFromBase();

			PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

			Intent intent=getActivity().getIntent();
			//Bundle bundle=intent.getBundleExtra("extrasBundle");
			m_bHideFormatSettings=intent.getBooleanExtra("readOnly", false)||Constants.MY_INFOSTART;

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

			((EditTextPreference)this.findPreference("server_user")).getEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

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
				customPref = (Preference) findPreference("server_address_for_wifi_connection");
				if (customPref != null) {
					removedPreferenceServerAddressForWifiConnection = customPref;
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

				if (m_wifiDescr != null) {
					// и установим значение
					//updatePrefServerWhenWifiFromBase();
					// сделать видимым
					if (removedPreferenceServerAddressForWifiConnection != null) {
						rootPreferences.addPreference(removedPreferenceServerAddressForWifiConnection);
						removedPreferenceServerAddressForWifiConnection = null;
					}
				} else {
					// сделать невидимым
					customPref = (Preference) findPreference("server_address_for_wifi_connection");
					if (customPref != null) {
						removedPreferenceServerAddressForWifiConnection = customPref;
						rootPreferences.removePreference(customPref);
					}
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
		public void onResume() {
			m_wifiDescr=WifiConnection.getWifiConnection(getActivity());
			updatePrefVisibility();
			super.onResume();
		}

		void updatePrefServerWhenWifiFromBase() {
			if (m_wifiDescr != null) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				String serverSetting = prefs.getString("server_address_for_wifi_connection", "");
				String serverSettingInBase = "";

				Cursor cursor = getActivity().getContentResolver().query(MTradeContentProvider.SERVERS_WHEN_WIFI_CONTENT_URI, new String[]{"server_address"}, "wifi_name=?", new String[]{m_wifiDescr}, null);
				if (cursor.moveToNext()) {
					serverSettingInBase = cursor.getString(0);
				}
				cursor.close();

				if (!serverSetting.equals(serverSettingInBase)) {
					// в это время сработает еще и onSharedPreferenceChanged, но ничего страшного не произойдет
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("server_address_for_wifi_connection", serverSettingInBase);
					editor.commit();
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
			if (key.equals("server_address_for_wifi_connection")) {
				String ftpServerAddress = prefs.getString("server_address_for_wifi_connection", "");
				if (m_wifiDescr != null) {
					ContentValues cv = new ContentValues();
					cv.put("wifi_name", m_wifiDescr);
					cv.put("server_address", ftpServerAddress);
					// getActivity() здесь может вернуть null, поэтому используем такой контекст
					Context context=Globals.getAppContext();
					ContentResolver contentResolver=context.getContentResolver();
					contentResolver.insert(MTradeContentProvider.SERVERS_WHEN_WIFI_CONTENT_URI, cv);
					//
				}
			}

		}
	}

}
