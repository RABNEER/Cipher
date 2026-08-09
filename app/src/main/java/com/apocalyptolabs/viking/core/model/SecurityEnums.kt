package com.apocalyptolabs.viking.core.model

enum class SenderType {
    BANK_SHORTCODE,
    TELECOM,
    UNKNOWN_NUMBER,
    INTERNATIONAL
}

enum class CallerType {
    DOMESTIC_KNOWN,
    DOMESTIC_UNKNOWN,
    TELEMARKETING_140,
    INTERNATIONAL_SUSPICIOUS,
    SPOOFED
}

enum class NfcRecordType {
    URL,
    TEXT,
    MIME,
    UNKNOWN
}

enum class AppCategory {
    GAME,
    UTILITY,
    UNKNOWN,
    FINANCE,
    COMMUNICATION
}
