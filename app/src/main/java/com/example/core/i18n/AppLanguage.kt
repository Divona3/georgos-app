package com.example.core.i18n

import java.util.Locale

data class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val localeTag: String,
    val regionHint: String
)

object SupportedLanguages {
    val all: List<AppLanguage> = listOf(
        AppLanguage("en", "English", "English", "en-US", "Universal"),
        AppLanguage("hi", "हिन्दी", "Hindi", "hi-IN", "India"),
        AppLanguage("ta", "தமிழ்", "Tamil", "ta-IN", "South Asia"),
        AppLanguage("te", "తెలుగు", "Telugu", "te-IN", "South Asia"),
        AppLanguage("mr", "मराठी", "Marathi", "mr-IN", "West India"),
        AppLanguage("bn", "বাংলা", "Bengali", "bn-IN", "East South Asia"),
        AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi", "pa-IN", "North India"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati", "gu-IN", "West India"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada", "kn-IN", "Karnataka"),
        AppLanguage("ml", "മലയാളം", "Malayalam", "ml-IN", "Kerala"),
        AppLanguage("es", "Español", "Spanish", "es-ES", "Americas & Europe"),
        AppLanguage("fr", "Français", "French", "fr-FR", "Africa & Europe"),
        AppLanguage("ar", "العربية", "Arabic", "ar-SA", "MENA"),
        AppLanguage("sw", "Kiswahili", "Swahili", "sw-KE", "East Africa"),
        AppLanguage("id", "Bahasa Indonesia", "Indonesian", "id-ID", "Southeast Asia"),
        AppLanguage("pt", "Português", "Portuguese", "pt-BR", "South America & Africa")
    )

    fun find(code: String): AppLanguage =
        all.firstOrNull { it.code == code } ?: all.first()

    fun toLocale(code: String): Locale =
        Locale.forLanguageTag(find(code).localeTag)
}
