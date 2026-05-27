package com.cypress.diary.storage

import com.cypress.diary.ui.navigation.AppModule

class AppModuleStore(
    private val preferences: PreferenceStore,
) {
    constructor(prefs: android.content.SharedPreferences) : this(SharedPreferencesPreferenceStore(prefs))

    fun load(): AppModule {
        val value = preferences.getString(KEY_MODULE, AppModule.Diary.name)
        return AppModule.selectable.firstOrNull { it.name == value } ?: AppModule.Diary
    }

    fun save(module: AppModule) {
        preferences.putString(KEY_MODULE, module.name)
    }

    companion object {
        private const val KEY_MODULE = "app_module"
    }
}
