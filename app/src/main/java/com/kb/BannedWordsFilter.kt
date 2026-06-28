package com.kb

object BannedWordsFilter {
    private val bannedWords = setOf(
        "spam", "xxx", "qqq"
        // Kendi yasaklı kelimelerinizi ekleyebilirsiniz
    )

    fun filter(text: String): String {
        var result = text
        bannedWords.forEach { banned ->
            result = result.replace(banned, "*".repeat(banned.length), ignoreCase = true)
        }
        return result
    }
}
