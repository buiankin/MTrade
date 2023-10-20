package ru.code22.mtrade;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class Globals extends Application {

	// Это чисто для того, чтобы получить контекст для выполнения запроса из PrefActivity, где нет контекста
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		RoboErrorReporter.bindReporter(this); // this, либо getApplicationContext()
		MySingleton.initInstance();

		// Make sure we use vector drawables
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

		Globals.context = getApplicationContext();
		//FirebaseApp.initializeApp(this);
	}

	public static Context getAppContext() {
		return Globals.context;
	}
	
}
