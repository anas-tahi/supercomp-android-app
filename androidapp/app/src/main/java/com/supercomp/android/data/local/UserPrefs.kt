package com.supercomp.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("user_prefs")

class UserPrefs(private val context: Context) {

    companion object {
        val TOKEN_KEY    = stringPreferencesKey("jwt_token")
        val USERNAME_KEY = stringPreferencesKey("username")
        val USER_ID_KEY  = stringPreferencesKey("user_id")
        val EMAIL_KEY    = stringPreferencesKey("email")
        val PROFILE_PICTURE_KEY = stringPreferencesKey("profile_picture")
        val PHONE_KEY = stringPreferencesKey("phone")
        val CITY_KEY = stringPreferencesKey("city")
    }

    suspend fun saveSession(token: String, username: String, userId: String, email: String = "", profilePicture: String = "", phone: String = "", city: String = "") {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY]    = token
            prefs[USERNAME_KEY] = username
            prefs[USER_ID_KEY]  = userId
            prefs[EMAIL_KEY]    = email
            prefs[PROFILE_PICTURE_KEY] = profilePicture
            prefs[PHONE_KEY] = phone
            prefs[CITY_KEY] = city
        }
    }

    suspend fun updateUsername(username: String) {
        context.dataStore.edit { prefs -> prefs[USERNAME_KEY] = username }
    }

    suspend fun updateProfilePicture(profilePicture: String) {
        context.dataStore.edit { prefs -> prefs[PROFILE_PICTURE_KEY] = profilePicture }
    }

    suspend fun updatePhone(phone: String) {
        context.dataStore.edit { prefs -> prefs[PHONE_KEY] = phone }
    }

    suspend fun updateCity(city: String) {
        context.dataStore.edit { prefs -> prefs[CITY_KEY] = city }
    }

    suspend fun getToken(): String?    = context.dataStore.data.first()[TOKEN_KEY]
    suspend fun getUsername(): String? = context.dataStore.data.first()[USERNAME_KEY]
    suspend fun getUserId(): String?   = context.dataStore.data.first()[USER_ID_KEY]
    suspend fun getEmail(): String?    = context.dataStore.data.first()[EMAIL_KEY]
    suspend fun getProfilePicture(): String? = context.dataStore.data.first()[PROFILE_PICTURE_KEY]
    suspend fun getPhone(): String? = context.dataStore.data.first()[PHONE_KEY]
    suspend fun getCity(): String? = context.dataStore.data.first()[CITY_KEY]

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
