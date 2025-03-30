package ru.code22.mtrade

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.multidex.MultiDexApplication



class Globals : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        RoboErrorReporter.bindReporter(this) // this, либо getApplicationContext()
        MySingleton.initInstance()

        // Make sure we use vector drawables
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        appContext = getApplicationContext()
        //FirebaseApp.initializeApp(this);
    }

    companion object {
        // Это чисто для того, чтобы получить контекст для выполнения запроса из PrefActivity, где нет контекста
        lateinit var appContext: Context
            private set
    }
}
