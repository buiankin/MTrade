package ru.code22.mtrade;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class WifiConnection {

	static public String getWifiConnection(Context context)
	{
		// Это более свежий код, проверяем, что версия ниже 8.1 (в этом случае не требуются права)
		if (Build.VERSION.SDK_INT< Build.VERSION_CODES.O_MR1) {
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			// чтобы SSID не был Unknown, у андроида 8.1+ должно быть право ACCESS_COARSE_LOCATION
			// а у API 29 (Android 10) так вообще ACCESS_FINE_LOCATION
			if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED && wifiInfo.getSSID() != null) {
				return StringUtils.removeQuotes(wifiInfo.getSSID());
			}
		}

		// Это более старый код, который, однако, работает на более свежих андроидах, на 8.1 в частности
		// (update: на 10-м и 9-м андроиде не работает совсем, т.е. ni.getExtraInfo() возвращает null)
		// и не надо при этом включать GPS в настройках Андроида
		// (чем-то он плох, не помню чем, на каких-то телефонах не давал результата)
		// Проверим, установлено ли WiFi соединение
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			if (netInfo != null) {
				for (NetworkInfo ni : netInfo) {
					// WIFI, MOBILE
					if (ni.getTypeName().equalsIgnoreCase("WIFI"))
						if (ni.isConnected()) {
							String wifiDescr = ni.getExtraInfo();
							if (wifiDescr!=null) {
								//Log.d(this.toString(), "test: wifi connection found");
								return StringUtils.removeQuotes(wifiDescr);
							}
						}
				}
			}
		}

		// А теперь еще раз новый код, если предыдущее не сработало (возможно такое?)
		// зато есть разрешение, и это будет работать на свежих андроидах
		if (ContextCompat.checkSelfPermission(context,
			Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			// чтобы SSID не был Unknown, у андроида 8.1+ должно быть право ACCESS_COARSE_LOCATION
			// а у API 29 (Android 10) так вообще ACCESS_FINE_LOCATION
			if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED && wifiInfo.getSSID() != null) {
				return StringUtils.removeQuotes(wifiInfo.getSSID());
			}

		}
		return null;

	}
}
