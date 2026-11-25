package com.example.data.model

enum class CropAspectRatio(
    val displayName: String,
    val shortLabel: String,
    val ratio: Float?
) {
    ORIGINAL("Original (Asli)", "Original", null),
    FREE("Free Crop (Bebas)", "Free", null),
    SQUARE("1:1 (Square)", "1:1", 1f / 1f),
    RATIO_4_3("4:3", "4:3", 4f / 3f),
    RATIO_3_4("3:4", "3:4", 3f / 4f),
    RATIO_16_9("16:9", "16:9", 16f / 9f),
    RATIO_9_16("9:16", "9:16", 9f / 16f),
    RATIO_4_5("4:5", "4:5", 4f / 5f),
    RATIO_5_4("5:4", "5:4", 5f / 4f),
    RATIO_3_2("3:2", "3:2", 3f / 2f),
    RATIO_2_3("2:3", "2:3", 2f / 3f)
}

data class NormalizedCropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
) {
    val width: Float get() = (right - left).coerceAtLeast(0.01f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.01f)

    fun isFullImage(): Boolean {
        return left <= 0.002f && top <= 0.002f && right >= 0.998f && bottom >= 0.998f
    }
}
