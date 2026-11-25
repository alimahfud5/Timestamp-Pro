package com.example.util

import android.graphics.Bitmap
import com.example.data.model.CropAspectRatio
import com.example.data.model.NormalizedCropRect
import kotlin.math.min
import kotlin.math.roundToInt

enum class CropDragHandle {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    MOVE
}

object CropUtils {

    private const val MIN_NORM_SIZE = 0.08f

    fun computeCenteredCropRect(
        imageWidth: Int,
        imageHeight: Int,
        aspectRatio: CropAspectRatio
    ): NormalizedCropRect {
        if (imageWidth <= 0 || imageHeight <= 0) return NormalizedCropRect()

        val targetRatio = aspectRatio.ratio
        if (aspectRatio == CropAspectRatio.ORIGINAL || targetRatio == null) {
            return if (aspectRatio == CropAspectRatio.FREE) {
                NormalizedCropRect(0.08f, 0.08f, 0.92f, 0.92f)
            } else {
                NormalizedCropRect(0f, 0f, 1f, 1f)
            }
        }

        val imgRatio = imageWidth.toFloat() / imageHeight.toFloat()
        return if (imgRatio > targetRatio) {
            val normW = (targetRatio / imgRatio).coerceIn(MIN_NORM_SIZE, 1f)
            val left = (1f - normW) / 2f
            NormalizedCropRect(left = left, top = 0f, right = left + normW, bottom = 1f)
        } else {
            val normH = (imgRatio / targetRatio).coerceIn(MIN_NORM_SIZE, 1f)
            val top = (1f - normH) / 2f
            NormalizedCropRect(left = 0f, top = top, right = 1f, bottom = top + normH)
        }
    }

    fun updateCropRectByDrag(
        current: NormalizedCropRect,
        handle: CropDragHandle,
        dxNorm: Float,
        dyNorm: Float,
        imageWidth: Int,
        imageHeight: Int,
        aspectRatio: CropAspectRatio
    ): NormalizedCropRect {
        if (imageWidth <= 0 || imageHeight <= 0) return current

        if (handle == CropDragHandle.MOVE) {
            val w = current.width
            val h = current.height
            val newLeft = (current.left + dxNorm).coerceIn(0f, 1f - w)
            val newTop = (current.top + dyNorm).coerceIn(0f, 1f - h)
            return NormalizedCropRect(
                left = newLeft,
                top = newTop,
                right = newLeft + w,
                bottom = newTop + h
            )
        }

        val targetRatio = aspectRatio.ratio
        if (targetRatio == null) {
            // Free / Original drag
            var l = current.left
            var t = current.top
            var r = current.right
            var b = current.bottom
            when (handle) {
                CropDragHandle.TOP_LEFT -> {
                    l = (l + dxNorm).coerceIn(0f, r - MIN_NORM_SIZE)
                    t = (t + dyNorm).coerceIn(0f, b - MIN_NORM_SIZE)
                }
                CropDragHandle.TOP_RIGHT -> {
                    r = (r + dxNorm).coerceIn(l + MIN_NORM_SIZE, 1f)
                    t = (t + dyNorm).coerceIn(0f, b - MIN_NORM_SIZE)
                }
                CropDragHandle.BOTTOM_LEFT -> {
                    l = (l + dxNorm).coerceIn(0f, r - MIN_NORM_SIZE)
                    b = (b + dyNorm).coerceIn(t + MIN_NORM_SIZE, 1f)
                }
                CropDragHandle.BOTTOM_RIGHT -> {
                    r = (r + dxNorm).coerceIn(l + MIN_NORM_SIZE, 1f)
                    b = (b + dyNorm).coerceIn(t + MIN_NORM_SIZE, 1f)
                }
                CropDragHandle.MOVE -> {}
            }
            return NormalizedCropRect(l, t, r, b)
        }

        // Fixed aspect ratio drag in pixel space:
        // (normW * imgW) / (normH * imgH) = targetRatio  =>  normW / normH = targetRatio / imgRatio
        val imgRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val normRatio = targetRatio / imgRatio
        val minNormW = maxOf(MIN_NORM_SIZE, MIN_NORM_SIZE * normRatio)

        return when (handle) {
            CropDragHandle.TOP_LEFT -> {
                val maxW = min(current.right, current.bottom * normRatio)
                val proposedW = current.width - dxNorm
                val newW = proposedW.coerceIn(minNormW.coerceAtMost(maxW), maxW)
                val newH = newW / normRatio
                NormalizedCropRect(
                    left = current.right - newW,
                    top = current.bottom - newH,
                    right = current.right,
                    bottom = current.bottom
                )
            }
            CropDragHandle.TOP_RIGHT -> {
                val maxW = min(1f - current.left, current.bottom * normRatio)
                val proposedW = current.width + dxNorm
                val newW = proposedW.coerceIn(minNormW.coerceAtMost(maxW), maxW)
                val newH = newW / normRatio
                NormalizedCropRect(
                    left = current.left,
                    top = current.bottom - newH,
                    right = current.left + newW,
                    bottom = current.bottom
                )
            }
            CropDragHandle.BOTTOM_LEFT -> {
                val maxW = min(current.right, (1f - current.top) * normRatio)
                val proposedW = current.width - dxNorm
                val newW = proposedW.coerceIn(minNormW.coerceAtMost(maxW), maxW)
                val newH = newW / normRatio
                NormalizedCropRect(
                    left = current.right - newW,
                    top = current.top,
                    right = current.right,
                    bottom = current.top + newH
                )
            }
            CropDragHandle.BOTTOM_RIGHT -> {
                val maxW = min(1f - current.left, (1f - current.top) * normRatio)
                val proposedW = current.width + dxNorm
                val newW = proposedW.coerceIn(minNormW.coerceAtMost(maxW), maxW)
                val newH = newW / normRatio
                NormalizedCropRect(
                    left = current.left,
                    top = current.top,
                    right = current.left + newW,
                    bottom = current.top + newH
                )
            }
            CropDragHandle.MOVE -> current
        }
    }

    fun cropBitmap(source: Bitmap, cropRect: NormalizedCropRect): Bitmap {
        if (cropRect.isFullImage()) return source

        val x = (cropRect.left.coerceIn(0f, 1f) * source.width).roundToInt().coerceIn(0, source.width - 1)
        val y = (cropRect.top.coerceIn(0f, 1f) * source.height).roundToInt().coerceIn(0, source.height - 1)
        val w = ((cropRect.right - cropRect.left).coerceIn(0.01f, 1f) * source.width)
            .roundToInt()
            .coerceIn(1, source.width - x)
        val h = ((cropRect.bottom - cropRect.top).coerceIn(0.01f, 1f) * source.height)
            .roundToInt()
            .coerceIn(1, source.height - y)

        return Bitmap.createBitmap(source, x, y, w, h)
    }
}
