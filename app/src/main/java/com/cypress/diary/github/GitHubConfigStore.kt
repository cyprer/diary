package com.cypress.diary.github

import android.content.Context

class GitHubConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): GitHubConfig? {
        val owner = prefs.getString(KEY_OWNER, "")?.trim().orEmpty()
        val repo = prefs.getString(KEY_REPO, "")?.trim().orEmpty()
        val branch = prefs.getString(KEY_BRANCH, DEFAULT_BRANCH)?.trim().orEmpty()
        val token = prefs.getString(KEY_TOKEN, "")?.trim().orEmpty()
        if (owner.isBlank() || repo.isBlank()) return null
        return GitHubConfig(owner = owner, repo = repo, branch = branch.ifBlank { DEFAULT_BRANCH }, token = token)
    }

    fun save(config: GitHubConfig) {
        prefs.edit()
            .putString(KEY_OWNER, config.owner.trim())
            .putString(KEY_REPO, config.repo.trim())
            .putString(KEY_BRANCH, config.branch.trim().ifBlank { DEFAULT_BRANCH })
            .putString(KEY_TOKEN, config.token.trim())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "github_config"
        private const val KEY_OWNER = "owner"
        private const val KEY_REPO = "repo"
        private const val KEY_BRANCH = "branch"
        private const val KEY_TOKEN = "token"
        const val DEFAULT_BRANCH = "main"
    }
}
