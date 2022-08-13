package com.example.auction.data
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val NAME ="logindeatils"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = NAME
)
class Logindatapreference (context: Context){
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")

    suspend fun savelogindetailsToPreferencesStore(username: String,password: String, context: Context) {
        context.dataStore.edit { preferences ->
            Log.v("MyActivity","bbbbbub")
            preferences[USERNAME] = username
            preferences[PASSWORD] = password
        }
    }

    val usernameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME] ?: ""
        }

    val passwordFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PASSWORD] ?: ""
        }

}