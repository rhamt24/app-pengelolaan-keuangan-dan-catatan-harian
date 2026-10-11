package com.example.data.model

enum class CurrencyType(val code: String, val symbol: String, val label: String) {
    IDR("IDR", "Rp", "Rupiah Indonesia (Rp)"),
    USD("USD", "$", "US Dollar ($)"),
    EUR("EUR", "€", "Euro (€)"),
    JPY("JPY", "¥", "Japanese Yen (¥)"),
    SGD("SGD", "S$", "Singapore Dollar (S$)"),
    MYR("MYR", "RM", "Malaysian Ringgit (RM)"),
    SAR("SAR", "﷼", "Saudi Riyal (﷼)"),
    GBP("GBP", "£", "British Pound (£)"),
    AUD("AUD", "A$", "Australian Dollar (A$)"),
    KRW("KRW", "₩", "Korean Won (₩)")
}

enum class AppLanguage(val code: String, val label: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    ENGLISH("en", "English")
}

enum class AppThemeMode(val code: String, val label: String) {
    SYSTEM("system", "Otomatis (Ikuti Sistem)"),
    LIGHT("light", "Tema Terang (Light)"),
    DARK("dark", "Tema Gelap (Dark)")
}
