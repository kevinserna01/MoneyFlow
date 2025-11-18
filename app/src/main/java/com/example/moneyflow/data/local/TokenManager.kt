package com.example.moneyflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_preferences")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_first_name")
        private val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }
    
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userFirstName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userFullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_FULL_NAME_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun saveUserFirstName(firstName: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = firstName
        }
    }

    suspend fun saveUserFullName(fullName: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_FULL_NAME_KEY] = fullName
        }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }
    
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_FULL_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }
    
    suspend fun getTokenSync(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }
    
    suspend fun getUserIdSync(): String? {
        return context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }
}
