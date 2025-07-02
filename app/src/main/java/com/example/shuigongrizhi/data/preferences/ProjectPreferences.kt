package com.example.shuigongrizhi.data.preferences

import android.content.Context
import android.content.SharedPreferences
// import dagger.hilt.android.qualifiers.ApplicationContext
// import javax.inject.Inject
// import javax.inject.Singleton

// @Singleton
class ProjectPreferences /* @Inject constructor(
    @ApplicationContext private val context: Context
) */ {
    private val context: Context? = null
    private val prefs: SharedPreferences? = context?.getSharedPreferences(
        "project_preferences",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_LAST_SELECTED_PROJECT_ID = "last_selected_project_id"
        private const val KEY_LAST_SELECTED_PROJECT_NAME = "last_selected_project_name"
    }
    
    fun saveLastSelectedProject(projectId: Long, projectName: String) {
        prefs?.edit()
            ?.putLong(KEY_LAST_SELECTED_PROJECT_ID, projectId)
            ?.putString(KEY_LAST_SELECTED_PROJECT_NAME, projectName)
            ?.apply()
    }
    
    fun getLastSelectedProjectId(): Long {
        return prefs?.getLong(KEY_LAST_SELECTED_PROJECT_ID, -1L) ?: -1L
    }
    
    fun getLastSelectedProjectName(): String? {
        return prefs?.getString(KEY_LAST_SELECTED_PROJECT_NAME, null)
    }
    
    fun clearLastSelectedProject() {
        prefs?.edit()
            ?.remove(KEY_LAST_SELECTED_PROJECT_ID)
            ?.remove(KEY_LAST_SELECTED_PROJECT_NAME)
            ?.apply()
    }
    
    fun hasLastSelectedProject(): Boolean {
        return getLastSelectedProjectId() != -1L
    }
}