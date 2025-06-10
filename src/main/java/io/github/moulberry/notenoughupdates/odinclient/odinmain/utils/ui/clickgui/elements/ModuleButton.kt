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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.loadBufferedImage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.ClickGUI
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.Panel
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.ColorAnimation
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.menu.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.clickGUIColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.moduleButtonColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.HoverHandler
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import net.minecraft.client.renderer.texture.DynamicTexture
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ModuleButton(val module: Module, val panel: Panel) {

    val menuElements: ArrayList<Element<*>> = ArrayList()

    val x: Float
        inline get() = panel.x

    var y: Float = 0f
        get() = field + panel.y

    private val colorAnim = ColorAnimation(150)

    val color: Color
        get() = colorAnim.get(clickGUIColor, moduleButtonColor, module.enabled).brighter(1 + hover.percent() / 500f)

    val width = Panel.WIDTH
    val height = 32f

    var extended = false

    private val extendAnim = EaseInOut(250)
    private val hoverHandler = HoverHandler(1000, 200)
    private val hover = HoverHandler(250)
    private val bannableIcon = DynamicTexture(loadBufferedImage("/assets/odinmain/clickgui/bannableIcon.png"))
    private val fpsHeavyIcon = DynamicTexture(loadBufferedImage("/assets/odinmain/clickgui/fpsHeavyIcon.png"))
    private val newFeatureIcon = DynamicTexture(loadBufferedImage("/assets/odinmain/clickgui/newFeatureIcon.png"))


    init {
        updateElements()
    }

    fun updateElements() {
        var position = -1 // This looks weird, but it starts at -1 because it gets incremented before being used.
        for (setting in module.settings) {
            /** Don't show hidden settings */
            if (setting.shouldBeVisible) run addElement@{
                position++
                if (menuElements.any { it.setting === setting }) return@addElement
                val newElement = when (setting) {
                    is BooleanSetting -> ElementCheckBox(this, setting)
                    is NumberSetting -> ElementSlider(this, setting)
                    is SelectorSetting -> ElementSelector(this, setting)
                    is StringSetting -> ElementTextField(this, setting)
                    is ColorSetting -> ElementColor(this, setting)
                    is ActionSetting -> ElementAction(this, setting)
                    is DualSetting -> ElementDual(this, setting)
                    is HudSetting -> ElementHud(this, setting)
                    is KeybindSetting -> io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.menu.ElementKeyBind(this, setting)
                    is DropdownSetting -> ElementDropdown(this, setting)
                    else -> {
                        position--
                        return@addElement
                    }
                }
                menuElements.add(position, newElement)
            } else {
                menuElements.removeAll {
                    it.setting === setting
                }
            }
        }
    }

    fun draw(): Float {
        var offs = height

        hoverHandler.handle(x, y, width, height - 1)
        hover.handle(x, y, width, height - 1)

        if (hoverHandler.percent() > 0) {
            ClickGUI.setDescription(module.desc, x + width + 10f, y, hoverHandler)
        }

        roundedRectangle(x, y, width, height, color)
        text(module.name, x + width / 2, y + height / 2, textColor, 14f, OdinFont.REGULAR, TextAlign.Middle)
        val textWidth = getTextWidth(module.name, 18f)

        if (textWidth < width - 80) {// too long text, not drawing symbol
            if (module.tag == Module.TagType.RISKY) {
                drawDynamicTexture(bannableIcon, x + width / 2 + textWidth / 2, y + 2f, 25f, 25f)
            } else if (module.tag == Module.TagType.FPSTAX) {
                drawDynamicTexture(fpsHeavyIcon, x + width / 2 + textWidth / 2, y, 35f, 35f)
            }
        }


        if (!extendAnim.isAnimating() && !extended || menuElements.isEmpty()) return offs

        var drawY = offs
        offs = height + floor(extendAnim.get(0f, getSettingHeight(), !extended))

        val scissor = scissor(x, y, width, offs)
        for (i in 0 until menuElements.size) {
            menuElements[i].y = drawY
            drawY += menuElements[i].render()
        }
        resetScissor(scissor)

        return offs
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isButtonHovered) {
            if (mouseButton == 0) {
                if (colorAnim.start()) module.toggle()
                return true
            } else if (mouseButton == 1) {
                if (menuElements.size > 0) {
                    if (extendAnim.start()) extended = !extended
                    if (!extended) {
                        menuElements.forEach {
                            it.listening = false
                        }
                    }
                }
                return true
            }
        } else if (isMouseUnderButton) {
            for (i in menuElements.size - 1 downTo 0) {
                if (menuElements[i].mouseClicked(mouseButton)) {
                    updateElements()
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (extended) {
            for (i in menuElements.size - 1 downTo 0) {
                menuElements[i].mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (i in menuElements.size - 1 downTo 0) {
                if (menuElements[i].keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    private val isButtonHovered: Boolean
        get() = isAreaHovered(x, y, width, height - 1)

    private val isMouseUnderButton: Boolean
        get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float {
        var totalHeight = 0f
        for (i in menuElements) totalHeight += i.h
        return totalHeight
    }
}
