package com.example.data.model

data class StampConfig(
    val source: TimestampSource = TimestampSource.CURRENT,
    val manualTimestamp: Long = System.currentTimeMillis(),
    val originalExifTimestamp: Long? = null,
    val dateFormatPattern: String = "d MMM yyyy HH.mm.ss 'WIB'",
    val position: StampPosition = StampPosition.BOTTOM_RIGHT,
    val textAlign: StampTextAlign = StampTextAlign.RIGHT,
    val fontSizeSp: Float = StampFontSize.NORMAL.sizeSp,
    val textColorHex: Long = 0xFFFFFFFF, // Default Putih (White)
    val fontFamily: StampFontFamily = StampFontFamily.MONOSPACE,
    val backgroundStyle: StampBackgroundStyle = StampBackgroundStyle.NONE,
    val backgroundOpacity: Float = 0.80f,
    val hasShadow: Boolean = true,
    val showLineIcons: Boolean = false,
    
    // Metadata fields (lokasi & GPS default mengikuti deteksi GPS saat ini)
    val includeLocation: Boolean = true,
    val locationName: String = "",
    val includeGps: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    
    val includeActivity: Boolean = false,
    val activityName: String = "",
    val includeOfficer: Boolean = false,
    val officerName: String = "",
    val includeNotes: Boolean = false,
    val notes: String = "",
    
    val badgeType: BadgeType = BadgeType.NONE,
    val marginDp: Int = 16,

    // Custom / default logo settings (default kanan atas)
    val logoUri: String? = DEFAULT_LOGO_URI,
    val logoPosition: LogoPosition = LogoPosition.TOP_RIGHT,
    val logoSizePercent: Float = 0.14f
) {
    fun getResolvedTimestamp(): Long {
        return when (source) {
            TimestampSource.CURRENT -> System.currentTimeMillis()
            TimestampSource.ORIGINAL_EXIF -> originalExifTimestamp ?: System.currentTimeMillis()
            TimestampSource.MANUAL -> manualTimestamp
        }
    }

    companion object {
        const val DEFAULT_LOGO_URI = "default_logo"
        val DEFAULT = StampConfig()

        val PRESET_SEMARANG_SELATAN = StampConfig(
            source = TimestampSource.CURRENT,
            dateFormatPattern = "d MMM yyyy HH.mm.ss 'WIB'",
            position = StampPosition.BOTTOM_RIGHT,
            textAlign = StampTextAlign.RIGHT,
            fontSizeSp = 15f,
            textColorHex = 0xFF00E5FF, // Cyan Pro
            fontFamily = StampFontFamily.MONOSPACE,
            backgroundStyle = StampBackgroundStyle.GLASS_NAVY,
            backgroundOpacity = 0.85f,
            hasShadow = true,
            includeLocation = true,
            locationName = "7 Jalan Pandanaran 2\nMugassari\nKecamatan Semarang Selatan\nJawa Tengah",
            includeGps = true,
            latitude = -6.99042,
            longitude = 110.41781,
            altitude = 14.0,
            includeActivity = true,
            activityName = "Patroli Kamtibmas Polsek Semarang Selatan",
            includeOfficer = true,
            officerName = "Piket Fungsi Polsek Semarang Selatan",
            includeNotes = false,
            badgeType = BadgeType.POLICE_SECURITY
        )

        val PRESET_PATROLI = PRESET_SEMARANG_SELATAN

        val PRESET_PROYEK = StampConfig(
            source = TimestampSource.CURRENT,
            dateFormatPattern = "dd/MM/yyyy HH:mm:ss 'WIB'",
            position = StampPosition.BOTTOM_RIGHT,
            fontSizeSp = 16f,
            textColorHex = 0xFFFFEB3B, // Yellow
            fontFamily = StampFontFamily.SANS_SERIF,
            backgroundStyle = StampBackgroundStyle.SOLID_DARK,
            backgroundOpacity = 0.80f,
            hasShadow = true,
            includeLocation = true,
            locationName = "Lokasi Proyek Lapangan",
            includeGps = true,
            includeActivity = true,
            activityName = "Progres Pembangunan Fisik Tahap 2",
            includeOfficer = true,
            officerName = "Pengawas Lapangan (QC)",
            includeNotes = false,
            badgeType = BadgeType.SHIELD_PRO
        )

        val PRESET_DOKUMENTASI_RESMI = StampConfig(
            source = TimestampSource.ORIGINAL_EXIF,
            dateFormatPattern = "yyyy-MM-dd HH:mm:ss",
            position = StampPosition.TOP_LEFT,
            fontSizeSp = 15f,
            textColorHex = 0xFFFFFFFF, // White
            fontFamily = StampFontFamily.BOLD_HEAVY,
            backgroundStyle = StampBackgroundStyle.ROUNDED_PILL,
            backgroundOpacity = 0.75f,
            hasShadow = true,
            includeLocation = true,
            includeGps = true,
            includeActivity = true,
            activityName = "Dokumentasi Berita Acara & Inspeksi",
            includeOfficer = true,
            officerName = "Petugas Lapangan",
            includeNotes = false,
            badgeType = BadgeType.CLOCK_GPS
        )

        val PRESET_MINIMALIS = StampConfig(
            source = TimestampSource.CURRENT,
            dateFormatPattern = "dd MMM yyyy, HH:mm",
            position = StampPosition.BOTTOM_RIGHT,
            fontSizeSp = 14f,
            textColorHex = 0xFFFFFFFF,
            fontFamily = StampFontFamily.SANS_SERIF,
            backgroundStyle = StampBackgroundStyle.NONE,
            backgroundOpacity = 0f,
            hasShadow = true,
            includeLocation = true,
            includeGps = false,
            includeActivity = false,
            includeOfficer = false,
            includeNotes = false,
            badgeType = BadgeType.NONE
        )

        val PRESET_TIMEMARK = StampConfig(
            source = TimestampSource.CURRENT,
            dateFormatPattern = "dd/MM/yyyy HH:mm",
            position = StampPosition.BOTTOM_LEFT,
            textAlign = StampTextAlign.LEFT,
            fontSizeSp = 15f,
            textColorHex = 0xFFFFFFFF,
            fontFamily = StampFontFamily.TIMEMARK,
            backgroundStyle = StampBackgroundStyle.NONE,
            backgroundOpacity = 0.80f,
            hasShadow = true,
            showLineIcons = false,
            includeLocation = true,
            includeGps = false,
            includeActivity = false,
            includeOfficer = false,
            includeNotes = false,
            badgeType = BadgeType.NONE
        )

        val PRESET_TIMEMARK_AKURAT = StampConfig(
            source = TimestampSource.CURRENT,
            dateFormatPattern = "dd/MM/yyyy HH:mm",
            position = StampPosition.BOTTOM_LEFT,
            textAlign = StampTextAlign.LEFT,
            fontSizeSp = 15f,
            textColorHex = 0xFFFFFFFF,
            fontFamily = StampFontFamily.TIMEMARK,
            backgroundStyle = StampBackgroundStyle.NONE,
            backgroundOpacity = 0.80f,
            hasShadow = true,
            showLineIcons = false,
            includeLocation = true,
            includeGps = false,
            includeActivity = false,
            includeOfficer = false,
            includeNotes = false,
            badgeType = BadgeType.NONE
        )
    }
}
