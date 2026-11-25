package com.example

import com.example.data.model.StampBackgroundStyle
import com.example.data.model.StampConfig
import com.example.data.model.StampFontFamily
import com.example.data.model.StampPosition
import com.example.data.model.TimestampSource
import com.example.util.StampRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTimestampSourceResolution() {
        val fixedMillis = 1700000000000L
        val config = StampConfig(
            source = TimestampSource.MANUAL,
            manualTimestamp = fixedMillis
        )
        assertEquals(fixedMillis, config.getResolvedTimestamp())
    }

    @Test
    fun testExifTimestampResolution() {
        val exifMillis = 1690000000000L
        val config = StampConfig(
            source = TimestampSource.ORIGINAL_EXIF,
            originalExifTimestamp = exifMillis
        )
        assertEquals(exifMillis, config.getResolvedTimestamp())
    }

    @Test
    fun testPrepareLinesWithAllFields() {
        val config = StampConfig(
            source = TimestampSource.MANUAL,
            manualTimestamp = 1700000000000L,
            dateFormatPattern = "yyyy-MM-dd HH:mm:ss",
            includeLocation = true,
            locationName = "Semarang Selatan",
            includeGps = true,
            latitude = -7.00514,
            longitude = 110.43812,
            altitude = 45.0,
            includeActivity = true,
            activityName = "Patroli Siang",
            includeOfficer = true,
            officerName = "Bripka Santoso",
            includeNotes = true,
            notes = "Aman kondusif"
        )

        val lines = StampRenderer.prepareLines(config)
        assertTrue(lines.size >= 5)
        assertTrue(lines[0].isHeader)
        assertTrue(lines.any { it.text.contains("Semarang Selatan") })
        assertTrue(lines.any { it.text.contains("GPS: -7.00514, 110.43812") })
        assertTrue(lines.any { it.text.contains("Patroli Siang") })
        assertTrue(lines.any { it.text.contains("Bripka Santoso") })
        assertTrue(lines.any { it.text.contains("Aman kondusif") })
    }

    @Test
    fun testMultiLineAddress() {
        val multiLineAddress = "Gg XIV\nLamper Tengah\nKecamatan Semarang Selatan\nJawa Tengah"
        val config = StampConfig(
            source = TimestampSource.MANUAL,
            manualTimestamp = 1794217517000L,
            dateFormatPattern = "d MMM yyyy HH.mm.ss 'WIB'",
            includeLocation = true,
            locationName = multiLineAddress
        )
        val lines = StampRenderer.prepareLines(config)
        assertTrue(lines.any { it.text.contains("Gg XIV") })
        assertTrue(lines.any { it.text.contains("Lamper Tengah") })
        assertTrue(lines.any { it.text.contains("Kecamatan Semarang Selatan") })
        assertTrue(lines.any { it.text.contains("Jawa Tengah") })
    }

    @Test
    fun testDefaultTextOrder() {
        // Default config starts ready for live GPS
        assertTrue(StampConfig.DEFAULT.includeLocation)
        assertTrue(StampConfig.DEFAULT.includeGps)
        assertEquals("", StampConfig.DEFAULT.locationName)
        assertEquals(null, StampConfig.DEFAULT.latitude)
        assertEquals(null, StampConfig.DEFAULT.longitude)

        // When live GPS populates default config, line order is: Tanggal Jam -> Jalan -> Kelurahan -> Kecamatan -> Provinsi -> Titik Koordinat
        val gpsPopulatedDefault = StampConfig.DEFAULT.copy(
            locationName = "Gg XIV\nLamper Tengah\nKecamatan Semarang Selatan\nJawa Tengah",
            latitude = -7.00180,
            longitude = 110.44080,
            altitude = 12.0
        )
        val lines = StampRenderer.prepareLines(gpsPopulatedDefault)
        assertEquals(6, lines.size)
        assertTrue(lines[0].isHeader)
        assertTrue(lines[1].text.contains("Gg XIV"))
        assertTrue(lines[2].text.contains("Lamper Tengah"))
        assertTrue(lines[3].text.contains("Kecamatan Semarang Selatan"))
        assertTrue(lines[4].text.contains("Jawa Tengah"))
        assertTrue(lines[5].text.contains("GPS: -7.00180, 110.44080"))
    }

    @Test
    fun testHideLineIcons() {
        val config = StampConfig(
            showLineIcons = false,
            includeLocation = true,
            locationName = "7 Jalan Pandanaran 2",
            includeGps = true,
            latitude = -6.99042,
            longitude = 110.41781,
            includeActivity = true,
            activityName = "Patroli",
            includeOfficer = true,
            officerName = "Petugas",
            includeNotes = true,
            notes = "Aman"
        )
        val lines = StampRenderer.prepareLines(config)
        assertTrue(lines.none { it.text.contains("📍") || it.text.contains("🌐") || it.text.contains("📋") || it.text.contains("👮") || it.text.contains("📝") })
        assertTrue(lines.any { it.text == "7 Jalan Pandanaran 2" })
        assertTrue(lines.any { it.text.startsWith("GPS:") })
    }

    @Test
    fun testPresets() {
        assertNotNull(StampConfig.PRESET_SEMARANG_SELATAN)
        assertNotNull(StampConfig.PRESET_PATROLI)
        assertNotNull(StampConfig.PRESET_PROYEK)
        assertNotNull(StampConfig.PRESET_DOKUMENTASI_RESMI)
        assertNotNull(StampConfig.PRESET_MINIMALIS)
        assertNotNull(StampConfig.PRESET_TIMEMARK)
        assertEquals(StampFontFamily.TIMEMARK, StampConfig.PRESET_TIMEMARK.fontFamily)
        assertTrue(StampFontFamily.values().contains(StampFontFamily.CONDENSED))
        assertEquals(StampPosition.BOTTOM_RIGHT, StampConfig.PRESET_PATROLI.position)
        assertEquals(StampPosition.BOTTOM_RIGHT, StampConfig.DEFAULT.position)
        assertEquals(com.example.data.model.StampTextAlign.RIGHT, StampConfig.DEFAULT.textAlign)
        assertEquals(0xFFFFFFFF, StampConfig.DEFAULT.textColorHex)
        assertEquals(StampBackgroundStyle.NONE, StampConfig.DEFAULT.backgroundStyle)
        assertEquals(com.example.data.model.StampFontSize.NORMAL.sizeSp, StampConfig.DEFAULT.fontSizeSp, 0.01f)
        assertEquals(com.example.data.model.BadgeType.NONE, StampConfig.DEFAULT.badgeType)
        assertEquals(false, StampConfig.DEFAULT.showLineIcons)
        assertEquals(com.example.data.model.LogoPosition.TOP_RIGHT, StampConfig.DEFAULT.logoPosition)
        assertNotNull(StampConfig.PRESET_TIMEMARK_AKURAT)
        assertEquals(true, StampConfig.PRESET_TIMEMARK_AKURAT.showTimemarkWatermark)
        assertEquals(false, StampConfig.PRESET_TIMEMARK_AKURAT.showLineIcons)
        assertEquals(StampPosition.BOTTOM_LEFT, StampConfig.PRESET_TIMEMARK_AKURAT.position)
        assertEquals(com.example.data.model.StampTextAlign.LEFT, StampConfig.PRESET_TIMEMARK_AKURAT.textAlign)
        assertEquals(StampBackgroundStyle.NONE, StampConfig.PRESET_TIMEMARK_AKURAT.backgroundStyle)
    }

    @Test
    fun testFontSizePresets() {
        val sizes = com.example.data.model.StampFontSize.values()
        assertEquals(6, sizes.size)
        assertEquals(
            listOf("Tiny", "Small", "Normal", "Medium", "Large", "Extra Large"),
            sizes.map { it.displayName }
        )
        assertEquals(com.example.data.model.StampFontSize.TINY, com.example.data.model.StampFontSize.fromSp(10f))
        assertEquals(com.example.data.model.StampFontSize.SMALL, com.example.data.model.StampFontSize.fromSp(12f))
        assertEquals(com.example.data.model.StampFontSize.NORMAL, com.example.data.model.StampFontSize.fromSp(15f))
        assertEquals(com.example.data.model.StampFontSize.MEDIUM, com.example.data.model.StampFontSize.fromSp(18f))
        assertEquals(com.example.data.model.StampFontSize.LARGE, com.example.data.model.StampFontSize.fromSp(22f))
        assertEquals(com.example.data.model.StampFontSize.EXTRA_LARGE, com.example.data.model.StampFontSize.fromSp(28f))
    }

    @Test
    fun testCropAspectRatiosAndMath() {
        val ratios = com.example.data.model.CropAspectRatio.values()
        assertEquals(11, ratios.size)
        assertEquals(
            listOf(
                "Original (Asli)",
                "Free Crop (Bebas)",
                "1:1 (Square)",
                "4:3",
                "3:4",
                "16:9",
                "9:16",
                "4:5",
                "5:4",
                "3:2",
                "2:3"
            ),
            ratios.map { it.displayName }
        )

        // Square crop on 1600x800 landscape image -> 800x800 centered (normW = 0.5, normH = 1.0)
        val squareRect = com.example.util.CropUtils.computeCenteredCropRect(
            1600,
            800,
            com.example.data.model.CropAspectRatio.SQUARE
        )
        assertEquals(0.25f, squareRect.left, 0.001f)
        assertEquals(0.75f, squareRect.right, 0.001f)
        assertEquals(0f, squareRect.top, 0.001f)
        assertEquals(1f, squareRect.bottom, 0.001f)

        // Drag bottom-right corner on 16:9 locked ratio keeps exact 16:9 pixel ratio
        val start169 = com.example.util.CropUtils.computeCenteredCropRect(
            1000,
            1000,
            com.example.data.model.CropAspectRatio.RATIO_16_9
        )
        val dragged169 = com.example.util.CropUtils.updateCropRectByDrag(
            current = start169,
            handle = com.example.util.CropDragHandle.BOTTOM_RIGHT,
            dxNorm = -0.2f,
            dyNorm = -0.2f,
            imageWidth = 1000,
            imageHeight = 1000,
            aspectRatio = com.example.data.model.CropAspectRatio.RATIO_16_9
        )
        val pixelRatio = (dragged169.width * 1000f) / (dragged169.height * 1000f)
        assertEquals(16f / 9f, pixelRatio, 0.01f)

        // Verify all fixed crop ratios on a 1200x1200 image
        val fixedRatios = listOf(
            com.example.data.model.CropAspectRatio.RATIO_4_3 to (4f / 3f),
            com.example.data.model.CropAspectRatio.RATIO_3_4 to (3f / 4f),
            com.example.data.model.CropAspectRatio.RATIO_16_9 to (16f / 9f),
            com.example.data.model.CropAspectRatio.RATIO_9_16 to (9f / 16f),
            com.example.data.model.CropAspectRatio.RATIO_4_5 to (4f / 5f),
            com.example.data.model.CropAspectRatio.RATIO_5_4 to (5f / 4f),
            com.example.data.model.CropAspectRatio.RATIO_3_2 to (3f / 2f),
            com.example.data.model.CropAspectRatio.RATIO_2_3 to (2f / 3f)
        )
        fixedRatios.forEach { (ratio, expectedRatio) ->
            val rect = com.example.util.CropUtils.computeCenteredCropRect(1200, 1200, ratio)
            assertEquals(expectedRatio, (rect.width * 1200f) / (rect.height * 1200f), 0.005f)
        }
    }

    @Test
    fun testBottomNavigationDestinations() {
        val destinations = com.example.ui.screens.MainDestination.values()
        assertEquals(3, destinations.size)
        assertEquals(
            listOf("Editor", "Template", "Pengaturan"),
            destinations.map { it.label }
        )
    }

    @Test
    fun testLogoRectAndVerificationCode() {
        // Default TOP_RIGHT placement on 1000x800 canvas with 200x100 logo at 20% width and 20px margin
        val rect = StampRenderer.computeLogoRect(
            targetWidth = 1000,
            targetHeight = 800,
            logoWidth = 200,
            logoHeight = 100,
            position = com.example.data.model.LogoPosition.TOP_RIGHT,
            sizeFraction = 0.20f,
            margin = 20f
        )
        assertEquals(780f, rect.left, 0.5f)
        assertEquals(20f, rect.top, 0.5f)
        assertEquals(980f, rect.right, 0.5f)
        assertEquals(120f, rect.bottom, 0.5f)

        // Verification code is deterministic and 14 uppercase alphanumeric characters
        val code1 = StampRenderer.generateVerificationCode(1795357464000L)
        val code2 = StampRenderer.generateVerificationCode(1795357464000L)
        assertEquals(14, code1.length)
        assertEquals(code1, code2)
        assertTrue(code1.all { it in '2'..'9' || it in 'A'..'Z' })
    }
}
