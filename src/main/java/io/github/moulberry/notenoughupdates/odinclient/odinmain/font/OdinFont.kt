package io.github.moulberry.notenoughupdates.odinclient.odinmain.font

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.TextAlign
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.TextPos
import kotlin.math.max

object OdinFont {

    const val REGULAR = 1
    const val BOLD = 2

    private val mc get() = Minecraft.getMinecraft()
    private val fontRenderer get() = mc.fontRendererObj

    private const val scaleFactor = 0.1f

    fun init() {
        // No initialization needed for vanilla font renderer
    }

    fun text(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        scale: Float,
        align: TextAlign = TextAlign.Left,
        verticalAlign: TextPos = TextPos.Middle,
        shadow: Boolean = false,
        type: Int = REGULAR,
    ) {
        if (color.isTransparent) return
        val actualScale = scale * scaleFactor
        val drawX = when (align) {
            TextAlign.Left -> x
            TextAlign.Right -> x - getTextWidth(text, actualScale)
            TextAlign.Middle -> x - getTextWidth(text, actualScale) / 2f
        }
        val drawY = when (verticalAlign) {
            TextPos.Top -> y
            TextPos.Middle -> y - getTextHeight(text, actualScale) / 2f
            TextPos.Bottom -> y - getTextHeight(text, actualScale)
        }
        val renderText = if (type == BOLD) "§l$text" else text
        if (actualScale == 1f) {
            fontRenderer.drawString(renderText, drawX, drawY, color.rgba, shadow)
        } else {
            GlStateManager.pushMatrix()
            GlStateManager.translate(drawX, drawY, 0f)
            GlStateManager.scale(actualScale, actualScale, 1f)
            fontRenderer.drawString(renderText, 0f, 0f, color.rgba, shadow)
            GlStateManager.popMatrix()
        }
    }

    fun getTextWidth(text: String, size: Float): Float {
        return fontRenderer.getStringWidth(text) * size * scaleFactor
    }

    fun getTextHeight(text: String, size: Float): Float {
        return fontRenderer.FONT_HEIGHT * size * scaleFactor
    }

    fun wrappedText(
        text: String,
        x: Float,
        y: Float,
        w: Float,
        color: Color,
        size: Float,
        type: Int = REGULAR,
        shadow: Boolean = false,
    ) {
        if (color.isTransparent) return
        val words = text.split(' ')
        var line = ""
        var currentHeight = y + 2
        for (word in words) {
            if (getTextWidth(line + word, size) > w) {
                text(line, x, currentHeight, color, size, type = type, shadow = shadow)
                line = "$word "
                currentHeight += getTextHeight(line, size + 7)
            } else {
                line += "$word "
            }
        }
        text(line, x, currentHeight, color, size, type = type, shadow = shadow)
    }

    fun wrappedTextBounds(text: String, width: Float, size: Float): Pair<Float, Float> {
        val words = text.split(' ')
        var line = ""
        var lines = 1
        var maxWidth = 0f
        for (word in words) {
            if (getTextWidth(line + word, size) > width) {
                maxWidth = max(maxWidth, getTextWidth(line, size))
                line = "$word "
                lines++
            } else {
                line += "$word "
            }
        }
        maxWidth = max(maxWidth, getTextWidth(line, size))
        return Pair(maxWidth, lines * getTextHeight(line, size + 3))
    }
}
