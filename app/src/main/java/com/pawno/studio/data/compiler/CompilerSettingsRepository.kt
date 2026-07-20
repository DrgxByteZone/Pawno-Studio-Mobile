package com.pawno.studio.data.compiler

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pawno_compiler_prefs")

data class CompilerSettingsData(
    val compilerVersionId: String = CompilerVersion.ZEEX_3_10_11.id,
    val debugLevel: Int = 2,
    val optimizationLevel: Int = 2,
    val stackReserveBytes: Int = 16384,
    val compactEncoding: Boolean = false,
    val compatibilityMode: Boolean = false,
    val treatWarningsAsErrors: Boolean = false,
    val requireSemicolons: Boolean = true,
    val requireParentheses: Boolean = true,
    val customFlags: String = "",
    val extraIncludePaths: List<String> = emptyList(),
    val lastOpenedFilePath: String? = null
) {
    val compilerVersion: CompilerVersion
        get() = CompilerVersion.fromId(compilerVersionId)
}

typealias CompilerSettings = CompilerSettingsData

class CompilerSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val COMPILER_VERSION = stringPreferencesKey("compiler_version")
        val DEBUG_LEVEL = intPreferencesKey("debug_level")
        val OPTIMIZATION_LEVEL = intPreferencesKey("optimization_level")
        val STACK_RESERVE = intPreferencesKey("stack_reserve")
        val COMPACT_ENCODING = booleanPreferencesKey("compact_encoding")
        val COMPATIBILITY_MODE = booleanPreferencesKey("compatibility_mode")
        val TREAT_WARNINGS_AS_ERRORS = booleanPreferencesKey("treat_warnings_as_errors")
        val REQUIRE_SEMICOLONS = booleanPreferencesKey("require_semicolons")
        val REQUIRE_PARENTHESES = booleanPreferencesKey("require_parentheses")
        val CUSTOM_FLAGS = stringPreferencesKey("custom_flags")
        val EXTRA_INCLUDES = stringPreferencesKey("extra_includes")
        val LAST_OPENED_FILE = stringPreferencesKey("last_opened_file")
    }

    val settingsFlow: Flow<CompilerSettingsData> = context.dataStore.data.map { preferences ->
        val version = preferences[PreferencesKeys.COMPILER_VERSION] ?: CompilerVersion.ZEEX_3_10_11.id
        val debug = preferences[PreferencesKeys.DEBUG_LEVEL] ?: 2
        val opt = preferences[PreferencesKeys.OPTIMIZATION_LEVEL] ?: 2
        val stack = preferences[PreferencesKeys.STACK_RESERVE] ?: 16384
        val compact = preferences[PreferencesKeys.COMPACT_ENCODING] ?: false
        val compat = preferences[PreferencesKeys.COMPATIBILITY_MODE] ?: false
        val warningsAsErrors = preferences[PreferencesKeys.TREAT_WARNINGS_AS_ERRORS] ?: false
        val semicolons = preferences[PreferencesKeys.REQUIRE_SEMICOLONS] ?: true
        val parentheses = preferences[PreferencesKeys.REQUIRE_PARENTHESES] ?: true
        val flags = preferences[PreferencesKeys.CUSTOM_FLAGS] ?: ""
        val includesStr = preferences[PreferencesKeys.EXTRA_INCLUDES] ?: ""
        val lastFile = preferences[PreferencesKeys.LAST_OPENED_FILE]

        val includesList = if (includesStr.isBlank()) emptyList() else includesStr.split(";")

        CompilerSettingsData(
            compilerVersionId = version,
            debugLevel = debug,
            optimizationLevel = opt,
            stackReserveBytes = stack,
            compactEncoding = compact,
            compatibilityMode = compat,
            treatWarningsAsErrors = warningsAsErrors,
            requireSemicolons = semicolons,
            requireParentheses = parentheses,
            customFlags = flags,
            extraIncludePaths = includesList,
            lastOpenedFilePath = lastFile
        )
    }

    suspend fun getSettings(): CompilerSettingsData {
        return settingsFlow.first()
    }

    suspend fun saveSettings(settings: CompilerSettingsData) {
        context.dataStore.edit {
            it[PreferencesKeys.COMPILER_VERSION] = settings.compilerVersionId
            it[PreferencesKeys.DEBUG_LEVEL] = settings.debugLevel
            it[PreferencesKeys.OPTIMIZATION_LEVEL] = settings.optimizationLevel
            it[PreferencesKeys.STACK_RESERVE] = settings.stackReserveBytes
            it[PreferencesKeys.COMPACT_ENCODING] = settings.compactEncoding
            it[PreferencesKeys.COMPATIBILITY_MODE] = settings.compatibilityMode
            it[PreferencesKeys.TREAT_WARNINGS_AS_ERRORS] = settings.treatWarningsAsErrors
            it[PreferencesKeys.REQUIRE_SEMICOLONS] = settings.requireSemicolons
            it[PreferencesKeys.REQUIRE_PARENTHESES] = settings.requireParentheses
            it[PreferencesKeys.CUSTOM_FLAGS] = settings.customFlags
            it[PreferencesKeys.EXTRA_INCLUDES] = settings.extraIncludePaths.joinToString(";")
        }
    }
}
