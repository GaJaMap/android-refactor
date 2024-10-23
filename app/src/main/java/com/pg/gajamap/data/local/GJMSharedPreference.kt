package com.pg.gajamap.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pg.gajamap.util.SortType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GJMSharedPreference @Inject constructor(@ApplicationContext context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = FILE_NAME)

    private val dataStore = context.dataStore


    val session: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SESSION_KEY] ?: ""
        }

    val email: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_EMAIL_KEY] ?: ""
        }

    val authority: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_AUTHORITY_KEY] ?: ""
        }

    val createdDate: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_CREATED_DATE_KEY] ?: ""
        }

    val isLogin: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_LOGIN_KEY] ?: false
        }

    val groupId: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[GROUP_ID_KEY] ?: -1L
        }

    val groupName: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[GROUP_NAME_KEY] ?: "전체"
        }

    val sortType: Flow<SortType> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortTypeValue = preferences[SORT_TYPE_KEY] ?: SortType.NEWEST.name
            SortType.valueOf(sortTypeValue)
        }

    val location: Flow<Pair<Double?, Double?>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val latitude = preferences[LATITUDE_KEY]
            val longitude = preferences[LONGITUDE_KEY]
            Pair(latitude, longitude)
        }

    suspend fun saveSession(session: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_KEY] = session
        }
    }

    suspend fun saveEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun saveAuthority(authority: String) {
        dataStore.edit { preferences ->
            preferences[USER_AUTHORITY_KEY] = authority
        }
    }

    suspend fun saveCreatedDate(createdDate: String) {
        dataStore.edit { preferences ->
            preferences[USER_CREATED_DATE_KEY] = createdDate
        }
    }

    suspend fun saveIsLogin(isLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGIN_KEY] = isLogin
        }
    }

    suspend fun saveGroupId(groupId: Long) {
        dataStore.edit { preferences ->
            preferences[GROUP_ID_KEY] = groupId
        }
    }

    suspend fun saveGroupName(groupName: String) {
        dataStore.edit { preferences ->
            preferences[GROUP_NAME_KEY] = groupName
        }
    }

    suspend fun saveSortType(sortType: SortType) {
        dataStore.edit { preferences ->
            preferences[SORT_TYPE_KEY] = sortType.name
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = latitude
            preferences[LONGITUDE_KEY] = longitude
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private const val FILE_NAME = "GJM"
        private val SESSION_KEY = stringPreferencesKey("SESSION")
        private val USER_EMAIL_KEY = stringPreferencesKey("USER_EMAIL")
        private val USER_AUTHORITY_KEY = stringPreferencesKey("USER_AUTHORITY")
        private val USER_CREATED_DATE_KEY = stringPreferencesKey("USER_CREATED_DATE")
        private val IS_LOGIN_KEY = booleanPreferencesKey("IS_LOGIN")
        private val GROUP_ID_KEY = longPreferencesKey("GROUP_ID")
        private val GROUP_NAME_KEY = stringPreferencesKey("GROUP_NAME")
        private val SORT_TYPE_KEY = stringPreferencesKey("SORT_TYPE")
        private val LATITUDE_KEY = doublePreferencesKey("latitude")
        private val LONGITUDE_KEY = doublePreferencesKey("longitude")
    }
}