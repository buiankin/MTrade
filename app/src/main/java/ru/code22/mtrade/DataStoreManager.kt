package ru.code22.mtrade

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// https://bytegoblin.io/blog/persistent-data-storage-using-datastore-preferences-in-jetpack-compose.mdx
// https://habr.com/ru/articles/874034/
// https://habr.com/ru/companies/tbank/articles/525010/
// по синтаксису больше всего подходит этот вариант
// https://developermemos.com/posts/migrating-from-sharedpreferences-to-datastore

// Здесь параметр context
// https://www.boltuix.com/2022/09/dark-mode-for-android-app-in-kotlin.html

// По умолчанию DARK
//<item>Dark</item>
//<item>Light</item>

// По умолчанию DEFAULT
//<item>RUR</item>
//<item>UAH</item>
//<item>KZT</item>
//<item>USD</item>



//private val Context.dataStore: DataStore<Preferences> = context.createDataStore(
//    name = "settings",
//    migrations = listOf(
//        SharedPreferencesMigration(context, "legacy_preferences_name")
//    )
//)

/*
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings", produceMigrations = { context->
    listOf(
        SharedPreferencesMigration(context, "${context.packageName}_preferences", setOf("app_theme", "currency"))
    )
})


//class DataStoreManager(private val dataStore1: DataStore<Preferences>) {
class DataStoreManager(private val context: Context) {

    // Еще один пример есть, у них здесь так
    // private val appContext = context.applicationContext
    // а потом в тексте, где у меня context, у них appContext

    val appThemeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[exampleKey] ?: "test"
        }

    suspend fun getAppTheme() = context.dataStore.data.map { preferences ->
        preferences[exampleKey] ?: "test"
    }.first()



    suspend fun setAppTheme(appTheme: String) = context.dataStore.edit { preferences ->
        preferences[exampleKey] = appTheme
    }



    companion object {
        private const val EXAMPLE_KEY = "app_theme"


    }

    private val exampleKey = stringPreferencesKey("$EXAMPLE_KEY")
}