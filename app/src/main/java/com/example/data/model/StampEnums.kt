package com.example.data.model

enum class TimestampSource(val displayName: String) {
    CURRENT("Waktu Sekarang"),
    ORIGINAL_EXIF("Waktu Asli Foto (EXIF)"),
    MANUAL("Waktu Manual")
}

enum class StampPosition(val displayName: String) {
    BOTTOM_LEFT("Kiri Bawah"),
    BOTTOM_RIGHT("Kanan Bawah"),
    TOP_LEFT("Kiri Atas"),
    TOP_RIGHT("Kanan Atas"),
    CENTER("Tengah")
}

enum class StampTextAlign(val displayName: String) {
    RIGHT("Rata Kanan"),
    LEFT("Rata Kiri"),
    CENTER("Rata Tengah")
}

enum class StampFontFamily(val displayName: String) {
    SANS_SERIF("Sans-Serif"),
    MONOSPACE("Monospace (Digital)"),
    SERIF("Serif"),
    BOLD_HEAVY("Huruf Tebal (Bold)"),
    CONDENSED("Condensed (Oswald)"),
    TIMEMARK("Timemark (Jam | Tanggal)")
}

enum class StampBackgroundStyle(val displayName: String) {
    GLASS_NAVY("Navy Glass (Transparan)"),
    SOLID_DARK("Hitam Pekat"),
    ROUNDED_PILL("Kotak Membulat"),
    OUTLINED("Garis Tepi (Outline)"),
    NONE("Tanpa Latar (Teks Polos)")
}

enum class BadgeType(val displayName: String) {
    SHIELD_PRO("Badge Pro"),
    CLOCK_GPS("Ikon Waktu"),
    POLICE_SECURITY("Patroli / Instansi"),
    NONE("Tanpa Ikon")
}

enum class StampFontSize(val displayName: String, val sizeSp: Float) {
    TINY("Tiny", 10f),
    SMALL("Small", 12f),
    NORMAL("Normal", 15f),
    MEDIUM("Medium", 18f),
    LARGE("Large", 22f),
    EXTRA_LARGE("Extra Large", 28f);

    companion object {
        fun fromSp(sp: Float): StampFontSize {
            return values().minByOrNull { kotlin.math.abs(it.sizeSp - sp) } ?: NORMAL
        }
    }
}

enum class LogoPosition(val displayName: String) {
    TOP_RIGHT("Kanan Atas"),
    TOP_LEFT("Kiri Atas"),
    BOTTOM_LEFT("Kiri Bawah"),
    BOTTOM_RIGHT("Kanan Bawah"),
    TOP_CENTER("Tengah Atas"),
    BOTTOM_CENTER("Tengah Bawah"),
    CENTER("Tengah")
}

enum class LogoSizePreset(val displayName: String, val widthFraction: Float) {
    SMALL("Kecil", 0.10f),
    MEDIUM("Sedang", 0.14f),
    LARGE("Besar", 0.20f),
    EXTRA_LARGE("Ekstra", 0.28f)
}

