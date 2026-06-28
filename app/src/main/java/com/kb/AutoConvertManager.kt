package com.kb

object AutoConvertManager {
    private val shortcuts = mapOf(
        "tşk" to "Teşekkürler!",
        "mslm" to "Merhaba, nasılsın?",
        "tmm" to "Tamam!",
        "gly" to "Geliyorum!",
        "gdy" to "Gidiyorum!",
        "ok" to "Okey!",
        "ty" to "Thank you!",
        "np" to "No problem!",
        "omw" to "On my way!",
        "brb" to "Be right back!"
    )

    fun get(shortcut: String): String? = shortcuts[shortcut.lowercase()]
}
