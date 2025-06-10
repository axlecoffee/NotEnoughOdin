/*
 * Copyright (C) 2025 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui


import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.ColorAnimation
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.menu.ElementTextField
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.buttonColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard

object SearchBar {

    var currentSearch = ""
    private var listening = false
    private val isHovered get() = MouseUtils.isAreaHovered(Minecraft.getMinecraft().displayWidth / 2f - 200f, Minecraft.getMinecraft().displayHeight - 100f, 400f, 30f)
    private val colorAnim = ColorAnimation(100)

    fun draw() {
        GlStateManager.pushMatrix()
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)

        GlStateManager.translate(Minecraft.getMinecraft().displayWidth / 2f, Minecraft.getMinecraft().displayHeight - 100f, 0f)
        roundedRectangle(-200f, 0f, 400f, 30f, ColorUtil.moduleButtonColor, 9f)
        if (listening || colorAnim.isAnimating()) {
            val color = colorAnim.get(ColorUtil.clickGUIColor, buttonColor, listening)
            rectangleOutline(-202f, -1f, 404f, 32f, color, 9f,3f)
        }
        if (currentSearch.isEmpty()) {
            text("Search here...", 0f, 18f, Colors.WHITE.withAlpha(0.5f), 18f, OdinFont.REGULAR, TextAlign.Middle)
        } else text(currentSearch, 0f, 12f, Colors.WHITE, 18f, OdinFont.REGULAR, TextAlign.Middle)
        GlStateManager.translate(-Minecraft.getMinecraft().displayWidth / 4f, -Minecraft.getMinecraft().displayHeight / 4f + 200f, 0f)
        GlStateManager.scale(scaleFactor, scaleFactor, 1f)
        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) listening = !listening
            return true
        } else if (listening) {
            if (colorAnim.start()) listening = false
        }
        return false
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> if (colorAnim.start()) listening = false
                Keyboard.KEY_BACK -> currentSearch = currentSearch.dropLast(1)
                !in ElementTextField.keyBlackList -> currentSearch += typedChar.toString()
            }
            if (currentSearch.length > "Auto-Renew Hollows Pass".length) currentSearch = currentSearch.dropLast(1)
            return true
        }
        return false
    }
}
