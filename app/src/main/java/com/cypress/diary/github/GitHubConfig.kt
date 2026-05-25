package com.cypress.diary.github

data class GitHubConfig(
    val owner: String,
    val repo: String,
    val branch: String,
    val token: String,
) {
    fun normalized(): GitHubConfig {
        return copy(
            owner = owner.trim(),
            repo = repo.trim(),
            branch = branch.trim().ifBlank { "main" },
            token = token.trim(),
        )
    }
}
