package com.rudra.defineeasy.feature_dictionary.presentation.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import java.io.File
import java.io.FileOutputStream

object WordCardRenderer {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1080
    private const val PADDING = 80f
    private const val BRAND_TEXT = "DefineEasy"
    private const val BRAND_URL = "defineeasy.app"

    fun renderToBitmap(wordInfo: WordInfo): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                intArrayOf(
                    0xFF1A1A2E.toInt(),
                    0xFF16213E.toInt(),
                    0xFF0F3460.toInt()
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF533483.toInt()
            alpha = 40
        }
        canvas.drawCircle(CARD_WIDTH * 0.8f, CARD_HEIGHT * 0.2f, 300f, accentPaint)
        canvas.drawCircle(CARD_WIDTH * 0.15f, CARD_HEIGHT * 0.85f, 200f, accentPaint)

        var yPos = PADDING + 80f

        val wordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
            isFakeBoldText = true
        }
        canvas.drawText(wordInfo.word.uppercase(), PADDING, yPos, wordPaint)
        yPos += 60f

        if (wordInfo.phonetic.isNotBlank()) {
            val phoneticPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFB8B8D0.toInt()
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText(wordInfo.phonetic, PADDING, yPos + 40f, phoneticPaint)
            yPos += 100f
        }

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF533483.toInt()
            strokeWidth = 3f
        }
        yPos += 20f
        canvas.drawLine(PADDING, yPos, CARD_WIDTH - PADDING, yPos, dividerPaint)
        yPos += 60f

        val meaning = wordInfo.meanings.firstOrNull()
        if (meaning != null) {
            val posPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFE94560.toInt()
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                isFakeBoldText = true
            }
            canvas.drawText(meaning.partOfSpeech.uppercase(), PADDING, yPos, posPaint)
            yPos += 55f

            val def = meaning.definitions.firstOrNull()
            if (def != null) {
                val defPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFE0E0E0.toInt()
                    textSize = 38f
                    typeface = Typeface.DEFAULT
                }
                val maxWidth = CARD_WIDTH - (PADDING * 2)
                val lines = wrapText(def.definition, defPaint, maxWidth)
                for (line in lines) {
                    if (yPos > CARD_HEIGHT - 250f) break
                    canvas.drawText(line, PADDING, yPos, defPaint)
                    yPos += 52f
                }

                if (def.example.isNullOrBlank().not() && yPos < CARD_HEIGHT - 250f) {
                    yPos += 20f
                    val examplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFFB8B8D0.toInt()
                        textSize = 30f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    }
                    val exampleLines = wrapText("\"${def.example}\"", examplePaint, maxWidth)
                    for (line in exampleLines) {
                        if (yPos > CARD_HEIGHT - 250f) break
                        canvas.drawText(line, PADDING, yPos, examplePaint)
                        yPos += 44f
                    }
                }
            }
        }

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8888AA.toInt()
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            isFakeBoldText = true
        }
        canvas.drawText(BRAND_TEXT, PADDING, CARD_HEIGHT - PADDING - 40f, brandPaint)

        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6666AA.toInt()
            textSize = 24f
        }
        canvas.drawText(BRAND_URL, PADDING, CARD_HEIGHT - PADDING, urlPaint)

        return bitmap
    }

    fun shareCard(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "word_cards").also { it.mkdirs() }
        val imageFile = File(file, "defineeasy_card_${System.currentTimeMillis()}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share word card"))
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word
            else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
