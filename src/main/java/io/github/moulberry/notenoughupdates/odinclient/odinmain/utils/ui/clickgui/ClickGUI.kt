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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.display
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Category
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule
import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.roundedRectangle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.scaleFactor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.wrappedText
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.wrappedTextBounds
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Screen
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.menu.ElementColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.buttonColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.HoverHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import kotlin.math.sign

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Panel]
 */
object ClickGUI : Screen() {

    private val panels: ArrayList<Panel> = arrayListOf()

    private var anim = EaseInOut(700)
    private var open = false
    private var desc: Description = Description(null, 0f, 0f, null)

    @OptIn(ExperimentalStdlibApi::class)
    fun init() {
        for (category in Category.entries) {
            panels.add(Panel(category))
        }
    }
    override fun draw() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 200f)
        if (anim.isAnimating()) {
            //translate(0f, floor(anim.get(-10f, 0f, !open)))
            val alpha = anim.get(0.7f, 1f, !open)
            ColorUtil.moduleButtonColor.alphaFloat = alpha
            ColorUtil.clickGUIColor.alphaFloat = alpha
            Colors.WHITE.alphaFloat = alpha
        }

        for (i in 0 until panels.size) {
            panels[i].draw()
        }

        SearchBar.draw()
        desc.render()

        if (anim.isAnimating()) {
            ColorUtil.moduleButtonColor.alphaFloat = 1f
            ColorUtil.clickGUIColor.alphaFloat = 1f
            Colors.WHITE.alphaFloat = 1f
        }
        GlStateManager.translate(0f, 0f, -200f)
        GlStateManager.popMatrix()
    }

    override fun onScroll(amount: Int) {
        if (Mouse.getEventDWheel() != 0) {
            val actualAmount = amount.sign * 16
            for (i in panels.size - 1 downTo 0) {
                if (panels[i].handleScroll(actualAmount)) return
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (SearchBar.mouseClicked(mouseButton)) return
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mouseButton)) return
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(state)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (SearchBar.keyTyped(typedChar, keyCode)) return
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(typedChar, keyCode)) return
        }

        if (keyCode == ClickGUIModule.settings.last().value && !anim.isAnimating()) {
            Minecraft.getMinecraft().displayGuiScreen(null as GuiScreen?)
            if (Minecraft.getMinecraft().currentScreen == null) {
                Minecraft.getMinecraft().setIngameFocus()
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun initGui() {
        open = true
        anim.start(true)

        if (OpenGlHelper.shadersSupported && Minecraft.getMinecraft().renderViewEntity is EntityPlayer && ClickGUIModule.blur) {
            Minecraft.getMinecraft().entityRenderer.stopUseShader()
            Minecraft.getMinecraft().entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
        }

        for (panel in panels) {
            panel.x = ClickGUIModule.panelX[panel.category]!!.value
            panel.y = ClickGUIModule.panelY[panel.category]!!.value
            panel.extended = ClickGUIModule.panelExtended[panel.category]!!.enabled
            panel.moduleButtons.forEach { it.updateElements() }
        }
    }

    override fun onGuiClosed() {
        for (panel in panels.filter { it.extended }.reversed()) {
            for (moduleButton in panel.moduleButtons.filter { it.extended }) {
                for (element in moduleButton.menuElements) {
                    if (element is ElementColor) {
                        element.dragging = null
                    }
                    element.listening = false
                }
            }
        }
        Config.save()

        open = false
        Minecraft.getMinecraft().entityRenderer.stopUseShader()
    }

    /**
     * Used to smooth transition between screens.
     */
    fun swapScreens(other: Screen) {
        // TODO: ACTUALLY MAKE THIS WORK
        display = other
    }

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float,  y: Float, hoverHandler: HoverHandler) {
        desc.text = text
        desc.x = x
        desc.y = y
        desc.hoverHandler = hoverHandler
    }

    /**
     * Used to render Descriptions
     * @see draw
     */
    data class Description(var text: String?, var x: Float, var y: Float, var hoverHandler: HoverHandler?) {

        /** Test whether a description is active or not */
        private val shouldRender: Boolean
            get() = text != null && hoverHandler != null && text != ""

        /** Handles rendering, if it's not active then it won't render */
        fun render() {
            if (!shouldRender) return
            val area = wrappedTextBounds(text!!, 300f, 12f)
            GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)
            roundedRectangle(
                x, y, area.first + 7, area.second + 9,
                buttonColor.withAlpha((hoverHandler!!.percent() / 100f).coerceIn(0f, 0.8f)), 5f
            )
            wrappedText(text!!, x + 7f, y + 12f, 300f, textColor, 12f, OdinFont.REGULAR)
            if (hoverHandler!!.percent() == 0) {
                text = null
                hoverHandler = null
            }
            GlStateManager.scale(scaleFactor, scaleFactor, 1f)
        }
    }
}
