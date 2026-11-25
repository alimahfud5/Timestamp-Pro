package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.BadgeType
import com.example.data.model.LogoPosition
import com.example.data.model.StampBackgroundStyle
import com.example.data.model.StampConfig
import com.example.data.model.StampFontFamily
import com.example.data.model.StampPosition
import com.example.data.model.StampTextAlign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

object StampRenderer {

    @Volatile
    private var cachedOswald: Typeface? = null

    @Volatile
    private var cachedRobotoCondensed: Typeface? = null

    private fun getOswaldTypeface(context: Context?): Typeface {
        cachedOswald?.let { return it }
        if (context != null) {
            try {
                val loaded = ResourcesCompat.getFont(context, R.font.oswald)
                if (loaded != null) {
                    cachedOswald = loaded
                    return loaded
                }
            } catch (_: Exception) {
            }
        }
        return Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }

    private fun getRobotoCondensedTypeface(context: Context?): Typeface {
        cachedRobotoCondensed?.let { return it }
        if (context != null) {
            try {
                val loaded = ResourcesCompat.getFont(context, R.font.roboto_condensed)
                if (loaded != null) {
                    cachedRobotoCondensed = loaded
                    return loaded
                }
            } catch (_: Exception) {
            }
        }
        return Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    }

    data class TextLine(
        val text: String,
        val isHeader: Boolean = false,
        val customColor: Int? = null
    )

    fun prepareLines(config: StampConfig): List<TextLine> {
        val lines = mutableListOf<TextLine>()

        // 1. Timestamp line
        val timeMillis = config.getResolvedTimestamp()
        val formattedTime = try {
            val sdf = SimpleDateFormat(config.dateFormatPattern, Locale("id", "ID"))
            sdf.format(Date(timeMillis))
        } catch (e: Exception) {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date(timeMillis))
        }
        lines.add(TextLine(formattedTime, isHeader = true))

        // 2. Location name (supports multi-line addresses)
        if (config.includeLocation && config.locationName.isNotBlank()) {
            val locLines = config.locationName.trim().split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            locLines.forEachIndexed { idx, lineText ->
                val prefix = if (config.showLineIcons) {
                    if (idx == 0) "📍 " else "   "
                } else ""
                lines.add(TextLine(prefix + lineText))
            }
        }

        // 3. GPS Coordinates
        if (config.includeGps && (config.latitude != null || config.longitude != null)) {
            val latStr = config.latitude?.let { String.format(Locale.US, "%.5f", it) } ?: "-"
            val lonStr = config.longitude?.let { String.format(Locale.US, "%.5f", it) } ?: "-"
            val altStr = config.altitude?.let { " (${String.format(Locale.US, "%.1f", it)}m)" } ?: ""
            val prefix = if (config.showLineIcons) "🌐 " else ""
            lines.add(TextLine("${prefix}GPS: $latStr, $lonStr$altStr"))
        }

        // 4. Activity Name
        if (config.includeActivity && config.activityName.isNotBlank()) {
            val prefix = if (config.showLineIcons) "📋 " else ""
            lines.add(TextLine(prefix + config.activityName.trim()))
        }

        // 5. Officer Name
        if (config.includeOfficer && config.officerName.isNotBlank()) {
            val prefix = if (config.showLineIcons) "👮 " else ""
            lines.add(TextLine(prefix + config.officerName.trim()))
        }

        // 6. Notes (supports multi-line notes)
        if (config.includeNotes && config.notes.isNotBlank()) {
            val noteLines = config.notes.trim().split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            noteLines.forEachIndexed { idx, lineText ->
                val prefix = if (config.showLineIcons) {
                    if (idx == 0) "📝 " else "   "
                } else ""
                lines.add(TextLine(prefix + lineText))
            }
        }

        return lines
    }

    fun renderStampOnBitmap(
        baseBitmap: Bitmap,
        config: StampConfig,
        context: Context? = null,
        logoBitmap: Bitmap? = null
    ): Bitmap {
        val mutableBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        drawStamp(canvas, mutableBitmap.width, mutableBitmap.height, config, context, logoBitmap)
        return mutableBitmap
    }

    fun drawStamp(
        canvas: Canvas,
        targetWidth: Int,
        targetHeight: Int,
        config: StampConfig,
        context: Context? = null,
        logoBitmap: Bitmap? = null
    ) {
        val scale = targetWidth / 1000f
        val margin = (config.marginDp * scale).coerceAtLeast(16f * scale)

        // Draw custom user logo if loaded (default: Kanan Atas)
        var topRightReservedY = 0f
        if (logoBitmap != null && !logoBitmap.isRecycled && logoBitmap.width > 0 && logoBitmap.height > 0) {
            val bounds = computeLogoRect(
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                logoWidth = logoBitmap.width,
                logoHeight = logoBitmap.height,
                position = config.logoPosition,
                sizeFraction = config.logoSizePercent,
                margin = margin
            )
            val logoRect = RectF(bounds.left, bounds.top, bounds.right, bounds.bottom)
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            canvas.drawBitmap(logoBitmap, srcRect, logoRect, logoPaint)
            if (config.logoPosition == LogoPosition.TOP_RIGHT) {
                topRightReservedY = bounds.bottom + 10f * scale
            }
        }

        // Draw Timemark Verified ("Timemark / Foto 100% akurat" + right-edge vertical verified) if enabled
        if (config.showTimemarkWatermark) {
            drawTimemarkWatermarks(
                canvas = canvas,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                config = config,
                context = context,
                topRightReservedY = topRightReservedY
            )
        }

        val lines = prepareLines(config)
        if (lines.isEmpty()) return

        if (config.fontFamily == StampFontFamily.TIMEMARK) {
            drawTimemarkStamp(canvas, targetWidth, targetHeight, config, lines, context)
            return
        }

        // Scale factors relative to canvas width (base reference width of 1000px)
        val baseFontSize = config.fontSizeSp * scale * 1.5f
        val headerFontSize = baseFontSize * 1.15f
        val paddingHorizontal = 24f * scale
        val paddingVertical = 18f * scale
        val lineSpacing = 10f * scale
        val cornerRadius = 14f * scale

        // Configure Paint for text
        val bodyTypeface = when (config.fontFamily) {
            StampFontFamily.MONOSPACE -> Typeface.MONOSPACE
            StampFontFamily.SANS_SERIF -> Typeface.SANS_SERIF
            StampFontFamily.SERIF -> Typeface.SERIF
            StampFontFamily.BOLD_HEAVY -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            StampFontFamily.CONDENSED -> getRobotoCondensedTypeface(context)
            StampFontFamily.TIMEMARK -> getRobotoCondensedTypeface(context)
        }

        val headerTypeface = when (config.fontFamily) {
            StampFontFamily.CONDENSED -> getOswaldTypeface(context)
            else -> Typeface.create(bodyTypeface, Typeface.BOLD)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = bodyTypeface
            this.color = config.textColorHex.toInt()
            if (config.hasShadow) {
                setShadowLayer(6f * scale, 2f * scale, 2f * scale, Color.argb(200, 0, 0, 0))
            }
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = headerTypeface
            this.color = config.textColorHex.toInt()
            if (config.hasShadow) {
                setShadowLayer(8f * scale, 3f * scale, 3f * scale, Color.argb(220, 0, 0, 0))
            }
        }

        // Measure text lines
        var maxLineWidth = 0f
        var totalTextHeight = 0f
        val lineHeights = mutableListOf<Float>()

        lines.forEachIndexed { index, line ->
            val p = if (line.isHeader) headerPaint else textPaint
            p.textSize = if (line.isHeader) headerFontSize else baseFontSize
            val width = p.measureText(line.text)
            if (width > maxLineWidth) {
                maxLineWidth = width
            }
            val fontMetrics = p.fontMetrics
            val h = fontMetrics.descent - fontMetrics.ascent
            lineHeights.add(h)
            totalTextHeight += h
            if (index < lines.size - 1) {
                totalTextHeight += lineSpacing
            }
        }

        // Include badge width if present
        val badgeSize = if (config.badgeType != BadgeType.NONE) headerFontSize * 1.1f else 0f
        val badgeSpacing = if (badgeSize > 0) 12f * scale else 0f
        val contentWidth = maxLineWidth + badgeSize + badgeSpacing
        val cardWidth = contentWidth + (paddingHorizontal * 2)
        val cardHeight = totalTextHeight + (paddingVertical * 2)

        val (startX, startY) = computeOrigin(
            position = config.position,
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            margin = margin
        )

        val cardRect = RectF(startX, startY, startX + cardWidth, startY + cardHeight)
        drawStampBackground(canvas, cardRect, cardHeight, cornerRadius, scale, config)

        // Draw Badge Icon if configured
        var currentY = startY + paddingVertical
        val textStartX = startX + paddingHorizontal
        val textRightX = startX + cardWidth - paddingHorizontal

        if (badgeSize > 0) {
            val badgeCenterX = when (config.textAlign) {
                StampTextAlign.RIGHT -> textRightX - (badgeSize / 2f)
                else -> textStartX + (badgeSize / 2f)
            }
            val badgeCenterY = currentY + (headerFontSize / 2f)
            drawBadgeIcon(canvas, config.badgeType, badgeCenterX, badgeCenterY, badgeSize, config.textColorHex.toInt(), scale)
        }

        // Draw Lines
        val textOffsetLeft = if (badgeSize > 0) badgeSize + badgeSpacing else 0f

        lines.forEachIndexed { index, line ->
            val p = if (line.isHeader) headerPaint else textPaint
            p.textSize = if (line.isHeader) headerFontSize else baseFontSize
            val fontMetrics = p.fontMetrics
            val baseline = currentY - fontMetrics.ascent
            val lineWidth = p.measureText(line.text)

            val lineX = when (config.textAlign) {
                StampTextAlign.RIGHT -> {
                    if (index == 0 && badgeSize > 0) {
                        textRightX - badgeSize - badgeSpacing - lineWidth
                    } else {
                        textRightX - lineWidth
                    }
                }
                StampTextAlign.CENTER -> {
                    textStartX + (contentWidth - lineWidth) / 2f
                }
                StampTextAlign.LEFT -> {
                    if (index == 0 && badgeSize > 0) {
                        textStartX + textOffsetLeft
                    } else {
                        textStartX
                    }
                }
            }

            canvas.drawText(line.text, lineX, baseline, p)
            currentY += lineHeights[index] + lineSpacing
        }
    }

    private fun drawTimemarkStamp(
        canvas: Canvas,
        targetWidth: Int,
        targetHeight: Int,
        config: StampConfig,
        lines: List<TextLine>,
        context: Context?
    ) {
        val scale = targetWidth / 1000f
        val baseFontSize = config.fontSizeSp * scale * 1.48f
        val clockFontSize = baseFontSize * 2.55f
        val metaFontSize = baseFontSize * 0.96f
        val paddingHorizontal = 24f * scale
        val paddingVertical = 18f * scale
        val lineSpacing = 8f * scale
        val headerBodyGap = 14f * scale
        val margin = (config.marginDp * scale).coerceAtLeast(16f * scale)
        val cornerRadius = 14f * scale

        val oswaldTypeface = getOswaldTypeface(context)
        val bodyTypeface = getRobotoCondensedTypeface(context)

        val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = Typeface.create(oswaldTypeface, Typeface.BOLD)
            this.textSize = clockFontSize
            this.color = config.textColorHex.toInt()
            if (config.hasShadow) {
                setShadowLayer(8f * scale, 2.5f * scale, 2.5f * scale, Color.argb(215, 0, 0, 0))
            }
        }

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = bodyTypeface
            this.textSize = metaFontSize
            this.color = config.textColorHex.toInt()
            if (config.hasShadow) {
                setShadowLayer(6f * scale, 2f * scale, 2f * scale, Color.argb(200, 0, 0, 0))
            }
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = bodyTypeface
            this.textSize = baseFontSize
            this.color = config.textColorHex.toInt()
            if (config.hasShadow) {
                setShadowLayer(6f * scale, 2f * scale, 2f * scale, Color.argb(200, 0, 0, 0))
            }
        }

        val localeId = Locale("id", "ID")
        val timeMillis = config.getResolvedTimestamp()
        val clockText = SimpleDateFormat("HH:mm", localeId).format(Date(timeMillis))
        val dateText = SimpleDateFormat("dd/MM/yyyy", localeId).format(Date(timeMillis))
        val dayText = SimpleDateFormat("EEEE", localeId).format(Date(timeMillis))

        val clockWidth = clockPaint.measureText(clockText)
        val clockFm = clockPaint.fontMetrics
        val clockVisualHeight = (-clockFm.ascent) * 0.82f

        val barGap = 14f * scale
        val barWidth = 3.5f * scale
        val metaWidth = max(metaPaint.measureText(dateText), metaPaint.measureText(dayText))
        val metaFm = metaPaint.fontMetrics
        val metaLineHeight = metaFm.descent - metaFm.ascent
        val metaStackGap = 6f * scale
        val metaBlockHeight = metaLineHeight * 2f + metaStackGap

        val headerCoreWidth = clockWidth + barGap + barWidth + barGap + metaWidth
        val headerHeight = max(clockVisualHeight, metaBlockHeight)

        val badgeSize = if (config.badgeType != BadgeType.NONE) baseFontSize * 1.4f else 0f
        val badgeSpacing = if (badgeSize > 0) 12f * scale else 0f
        val headerTotalWidth = headerCoreWidth + badgeSize + badgeSpacing

        val rawBodyLines = lines.drop(1)
        val maxAllowedBodyWidth = (targetWidth - margin * 2f - paddingHorizontal * 2f).coerceAtLeast(180f * scale)
        val bodyLines = mutableListOf<TextLine>()
        rawBodyLines.forEach { line ->
            wrapTextByWidth(line.text, bodyPaint, maxAllowedBodyWidth).forEach { wrapped ->
                bodyLines.add(TextLine(wrapped, line.isHeader, line.customColor))
            }
        }

        var maxBodyWidth = 0f
        var totalBodyHeight = 0f
        val bodyLineHeights = mutableListOf<Float>()

        bodyLines.forEachIndexed { index, line ->
            val w = bodyPaint.measureText(line.text)
            if (w > maxBodyWidth) maxBodyWidth = w
            val fm = bodyPaint.fontMetrics
            val h = fm.descent - fm.ascent
            bodyLineHeights.add(h)
            totalBodyHeight += h
            if (index < bodyLines.size - 1) {
                totalBodyHeight += lineSpacing
            }
        }

        val contentWidth = max(headerTotalWidth, maxBodyWidth)
        val totalContentHeight = headerHeight + (if (bodyLines.isNotEmpty()) headerBodyGap + totalBodyHeight else 0f)
        val cardWidth = contentWidth + paddingHorizontal * 2
        val cardHeight = totalContentHeight + paddingVertical * 2

        val (startX, startY) = computeOrigin(
            position = config.position,
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            margin = margin
        )

        val cardRect = RectF(startX, startY, startX + cardWidth, startY + cardHeight)
        drawStampBackground(canvas, cardRect, cardHeight, cornerRadius, scale, config)

        val textStartX = startX + paddingHorizontal
        val textRightX = startX + cardWidth - paddingHorizontal
        val headerTopY = startY + paddingVertical

        val headerBlockStartX = when (config.textAlign) {
            StampTextAlign.RIGHT -> textRightX - headerTotalWidth
            StampTextAlign.CENTER -> textStartX + (contentWidth - headerTotalWidth) / 2f
            StampTextAlign.LEFT -> textStartX
        }

        val clockStartX = if (badgeSize > 0 && config.textAlign != StampTextAlign.RIGHT) {
            headerBlockStartX + badgeSize + badgeSpacing
        } else {
            headerBlockStartX
        }

        if (badgeSize > 0) {
            val badgeCenterX = if (config.textAlign == StampTextAlign.RIGHT) {
                textRightX - badgeSize / 2f
            } else {
                headerBlockStartX + badgeSize / 2f
            }
            val badgeCenterY = headerTopY + headerHeight / 2f
            drawBadgeIcon(canvas, config.badgeType, badgeCenterX, badgeCenterY, badgeSize, config.textColorHex.toInt(), scale)
        }

        // Draw giant Oswald clock vertically centered in headerHeight
        val clockBaseline = headerTopY + (headerHeight + clockVisualHeight) / 2f
        canvas.drawText(clockText, clockStartX, clockBaseline, clockPaint)

        // Draw golden accent vertical divider bar
        val barX = clockStartX + clockWidth + barGap
        val barTop = clockBaseline - clockVisualHeight * 0.94f
        val barBottom = clockBaseline + clockVisualHeight * 0.04f
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.rgb(245, 184, 46) // Timemark Amber/Gold vertical line
            if (config.hasShadow) {
                setShadowLayer(4f * scale, 1.5f * scale, 1.5f * scale, Color.argb(180, 0, 0, 0))
            }
        }
        canvas.drawRoundRect(
            RectF(barX, barTop, barX + barWidth, barBottom),
            barWidth / 2f,
            barWidth / 2f,
            barPaint
        )

        // Draw stacked Date and Day beside the vertical bar
        val metaX = barX + barWidth + barGap
        val metaTopY = headerTopY + (headerHeight - metaBlockHeight) / 2f
        val dateBaseline = metaTopY - metaFm.ascent
        val dayBaseline = metaTopY + metaLineHeight + metaStackGap - metaFm.ascent
        canvas.drawText(dateText, metaX, dateBaseline, metaPaint)
        canvas.drawText(dayText, metaX, dayBaseline, metaPaint)

        // Draw body lines below header
        var currentY = headerTopY + headerHeight + headerBodyGap
        bodyLines.forEachIndexed { index, line ->
            val fm = bodyPaint.fontMetrics
            val baseline = currentY - fm.ascent
            val lineWidth = bodyPaint.measureText(line.text)

            val lineX = when (config.textAlign) {
                StampTextAlign.RIGHT -> textRightX - lineWidth
                StampTextAlign.CENTER -> textStartX + (contentWidth - lineWidth) / 2f
                StampTextAlign.LEFT -> textStartX
            }

            canvas.drawText(line.text, lineX, baseline, bodyPaint)
            currentY += bodyLineHeights[index] + lineSpacing
        }
    }

    private fun computeOrigin(
        position: StampPosition,
        targetWidth: Int,
        targetHeight: Int,
        cardWidth: Float,
        cardHeight: Float,
        margin: Float
    ): Pair<Float, Float> {
        return when (position) {
            StampPosition.TOP_LEFT -> margin to margin
            StampPosition.TOP_RIGHT -> (targetWidth - cardWidth - margin) to margin
            StampPosition.BOTTOM_LEFT -> margin to (targetHeight - cardHeight - margin)
            StampPosition.BOTTOM_RIGHT -> (targetWidth - cardWidth - margin) to (targetHeight - cardHeight - margin)
            StampPosition.CENTER -> ((targetWidth - cardWidth) / 2f) to ((targetHeight - cardHeight) / 2f)
        }
    }

    private fun drawStampBackground(
        canvas: Canvas,
        cardRect: RectF,
        cardHeight: Float,
        cornerRadius: Float,
        scale: Float,
        config: StampConfig
    ) {
        val alphaInt = (config.backgroundOpacity.coerceIn(0f, 1f) * 255).toInt()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (config.backgroundStyle) {
            StampBackgroundStyle.GLASS_NAVY -> {
                bgPaint.style = Paint.Style.FILL
                bgPaint.color = Color.argb(alphaInt, 6, 17, 34)
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f * scale
                    color = Color.argb((alphaInt * 0.9f).toInt(), 0, 229, 255)
                }
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)
            }
            StampBackgroundStyle.SOLID_DARK -> {
                bgPaint.style = Paint.Style.FILL
                bgPaint.color = Color.argb(alphaInt, 12, 12, 12)
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)
            }
            StampBackgroundStyle.ROUNDED_PILL -> {
                bgPaint.style = Paint.Style.FILL
                bgPaint.color = Color.argb(alphaInt, 15, 25, 45)
                val pillRadius = cardHeight / 2f
                canvas.drawRoundRect(cardRect, pillRadius, pillRadius, bgPaint)
            }
            StampBackgroundStyle.OUTLINED -> {
                bgPaint.style = Paint.Style.FILL
                bgPaint.color = Color.argb((alphaInt * 0.4f).toInt(), 5, 11, 20)
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 3f * scale
                    color = config.textColorHex.toInt()
                }
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)
            }
            StampBackgroundStyle.NONE -> {
                // No background, direct text rendering
            }
        }
    }

    private fun drawBadgeIcon(
        canvas: Canvas,
        badgeType: BadgeType,
        cx: Float,
        cy: Float,
        badgeSize: Float,
        colorInt: Int,
        scale: Float
    ) {
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }
        when (badgeType) {
            BadgeType.SHIELD_PRO -> drawShield(canvas, cx, cy, badgeSize * 0.9f, badgePaint, scale)
            BadgeType.CLOCK_GPS -> drawClock(canvas, cx, cy, badgeSize * 0.85f, badgePaint, scale)
            BadgeType.POLICE_SECURITY -> drawPoliceStar(canvas, cx, cy, badgeSize * 0.9f, badgePaint)
            BadgeType.NONE -> {}
        }
    }

    private fun drawShield(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint, scale: Float) {
        val half = size / 2f
        val path = Path().apply {
            moveTo(cx, cy - half)
            lineTo(cx + half, cy - half * 0.5f)
            lineTo(cx + half, cy + half * 0.2f)
            quadTo(cx, cy + half, cx, cy + half)
            quadTo(cx, cy + half, cx - half, cy + half * 0.2f)
            lineTo(cx - half, cy - half * 0.5f)
            close()
        }
        val prevStyle = paint.style
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * scale
        canvas.drawPath(path, paint)
        paint.style = prevStyle
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint, scale: Float) {
        val radius = size / 2f
        val prevStyle = paint.style
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * scale
        canvas.drawCircle(cx, cy, radius, paint)
        canvas.drawLine(cx, cy, cx, cy - radius * 0.6f, paint)
        canvas.drawLine(cx, cy, cx + radius * 0.5f, cy, paint)
        paint.style = prevStyle
    }

    private fun drawPoliceStar(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val radius = size / 2f
        val path = Path()
        val numPoints = 6
        val innerRadius = radius * 0.55f
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = i * Math.PI / numPoints - Math.PI / 2
            val x = (cx + r * Math.cos(angle)).toFloat()
            val y = (cy + r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        val prevStyle = paint.style
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        paint.style = prevStyle
    }

    data class LogoBounds(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )

    fun computeLogoRect(
        targetWidth: Int,
        targetHeight: Int,
        logoWidth: Int,
        logoHeight: Int,
        position: LogoPosition,
        sizeFraction: Float,
        margin: Float
    ): LogoBounds {
        val safeFraction = sizeFraction.coerceIn(0.05f, 0.45f)
        val drawW = (targetWidth * safeFraction).coerceAtLeast(24f)
        val aspect = logoHeight.toFloat() / logoWidth.toFloat().coerceAtLeast(1f)
        val drawH = (drawW * aspect).coerceAtMost(targetHeight * 0.45f)
        val finalW = if (drawW * aspect > targetHeight * 0.45f) drawH / aspect else drawW

        val left = when (position) {
            LogoPosition.TOP_LEFT, LogoPosition.BOTTOM_LEFT -> margin
            LogoPosition.TOP_RIGHT, LogoPosition.BOTTOM_RIGHT -> targetWidth - finalW - margin
            LogoPosition.TOP_CENTER, LogoPosition.BOTTOM_CENTER, LogoPosition.CENTER -> (targetWidth - finalW) / 2f
        }
        val top = when (position) {
            LogoPosition.TOP_LEFT, LogoPosition.TOP_RIGHT, LogoPosition.TOP_CENTER -> margin
            LogoPosition.BOTTOM_LEFT, LogoPosition.BOTTOM_RIGHT, LogoPosition.BOTTOM_CENTER -> targetHeight - drawH - margin
            LogoPosition.CENTER -> (targetHeight - drawH) / 2f
        }
        return LogoBounds(left, top, left + finalW, top + drawH)
    }

    fun generateVerificationCode(timeMillis: Long): String {
        val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        var seed = abs(timeMillis xor 0x5A3C9E1B7D4L)
        val sb = StringBuilder(14)
        for (i in 0 until 14) {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            val idx = ((seed ushr 32) and 0x7FFFFFFFL) % alphabet.length
            sb.append(alphabet[idx.toInt()])
        }
        return sb.toString()
    }

    private fun wrapTextByWidth(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (paint.measureText(text) <= maxWidth) return listOf(text)
        val words = text.split(" ")
        val result = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) result.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return if (result.isEmpty()) listOf(text) else result
    }

    private fun drawTimemarkWatermarks(
        canvas: Canvas,
        targetWidth: Int,
        targetHeight: Int,
        config: StampConfig,
        context: Context?,
        topRightReservedY: Float
    ) {
        val scale = targetWidth / 1000f
        val margin = (config.marginDp * scale).coerceAtLeast(18f * scale)
        val bodyTypeface = getRobotoCondensedTypeface(context)

        // 1. Top-Right "Timemark" (gold "Time" + white "mark") & "Foto 100% akurat"
        val brandSize = 31f * scale
        val subSize = 18f * scale

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = brandSize
            color = Color.rgb(245, 184, 46)
            setShadowLayer(4f * scale, 1.5f * scale, 1.5f * scale, Color.argb(190, 0, 0, 0))
        }
        val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = brandSize
            color = Color.WHITE
            setShadowLayer(4f * scale, 1.5f * scale, 1.5f * scale, Color.argb(190, 0, 0, 0))
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bodyTypeface
            textSize = subSize
            color = Color.argb(240, 255, 255, 255)
            setShadowLayer(4f * scale, 1.5f * scale, 1.5f * scale, Color.argb(190, 0, 0, 0))
        }

        val timeWidth = goldPaint.measureText("Time")
        val markWidth = markPaint.measureText("mark")
        val totalBrandW = timeWidth + markWidth
        val rightEdgeX = targetWidth - margin
        val topStartY = if (topRightReservedY > margin) topRightReservedY else margin

        val brandFm = goldPaint.fontMetrics
        val brandBaseline = topStartY - brandFm.ascent
        canvas.drawText("Time", rightEdgeX - totalBrandW, brandBaseline, goldPaint)
        canvas.drawText("mark", rightEdgeX - markWidth, brandBaseline, markPaint)

        val subText = "Foto 100% akurat"
        val subW = subPaint.measureText(subText)
        val subFm = subPaint.fontMetrics
        val subBaseline = brandBaseline + brandFm.descent + 4f * scale - subFm.ascent
        canvas.drawText(subText, rightEdgeX - subW, subBaseline, subPaint)

        // 2. Right-middle vertical edge watermark: [shield-check] CODE  Timemark Verified
        val verSize = 15.5f * scale
        val verPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bodyTypeface
            textSize = verSize
            color = Color.argb(225, 255, 255, 255)
            setShadowLayer(3f * scale, 1f * scale, 1f * scale, Color.argb(180, 0, 0, 0))
        }
        val code = generateVerificationCode(config.getResolvedTimestamp())
        val verLabel = "$code  Timemark Verified"
        val shieldSize = 16f * scale
        val shieldGap = 7f * scale
        val totalStripW = shieldSize + shieldGap + verPaint.measureText(verLabel)

        val anchorX = targetWidth - 16f * scale
        val anchorY = targetHeight * 0.5f

        canvas.save()
        canvas.translate(anchorX, anchorY)
        canvas.rotate(-90f)
        val stripStartX = -totalStripW / 2f
        val shieldCx = stripStartX + shieldSize / 2f
        val shieldCy = -verSize * 0.34f
        drawShield(canvas, shieldCx, shieldCy, shieldSize, Paint(verPaint), scale)
        // Small check mark inside shield
        val checkPaint = Paint(verPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.8f * scale
        }
        val checkPath = Path().apply {
            moveTo(shieldCx - shieldSize * 0.2f, shieldCy)
            lineTo(shieldCx - shieldSize * 0.04f, shieldCy + shieldSize * 0.16f)
            lineTo(shieldCx + shieldSize * 0.24f, shieldCy - shieldSize * 0.16f)
        }
        canvas.drawPath(checkPath, checkPaint)
        canvas.drawText(verLabel, stripStartX + shieldSize + shieldGap, 0f, verPaint)
        canvas.restore()
    }
}
