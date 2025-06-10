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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud

import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.ModuleManager.huds
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor.Companion.register
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.scaleFactor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Screen
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.sign

/**
 * Screen that renders all your active Hud's
 *
 * @author Stivais
 */
object EditHUDGui : Screen() {

    var dragging: HudElement? = null

    private var startX: Float = 0f
    private var startY: Float = 0f

    var open = false

    private val openAnim = EaseInOut(600)
    private val resetAnim = EaseInOut(1000)

    /** Code is horrible ngl but it looks nice */
    override fun draw() {
        Minecraft.getMinecraft().mcProfiler.startSection("Odin Example Hud")
        dragging?.let {
            it.x = MouseUtils.mouseX - startX
            it.y = MouseUtils.mouseY - startY
        }
        GlStateManager.pushMatrix()
        GlStateManager.scale(Minecraft.getMinecraft().displayWidth / 1920f, Minecraft.getMinecraft().displayHeight / 1080f, 0f)
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)

        if (openAnim.isAnimating()) {
            val animVal = openAnim.get(0f, 1f, !open)
            GlStateManager.scale(animVal, animVal, 0f)
        }

        if (openAnim.isAnimating()) {
            val animVal = openAnim.get(0f, 1f, !open)
            GlStateManager.scale(1f / animVal, 1f / animVal, 0f)
        }
        GlStateManager.scale(scaleFactor, scaleFactor, 1f)

        GlStateManager.popMatrix()

        if (!open) return
        for (i in 0 until huds.size) {
            huds[i].draw(example = true)
        }

        Minecraft.getMinecraft().mcProfiler.endSection()
    }

    override fun onScroll(amount: Int) {
        for (i in huds.size - 1 downTo 0) {
            if (huds[i].accept()) {
                huds[i].scale += amount.sign * 0.05f
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (i in huds.size - 1 downTo 0) {
            if (huds[i].accept()) {
                dragging = huds[i]
                huds[i].anim2.start()

                startX = MouseUtils.mouseX - huds[i].x
                startY = MouseUtils.mouseY - huds[i].y
                return
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging?.anim2?.start(true)
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun initGui() {
        openAnim.start()
        open = true
    }

    override fun onGuiClosed() {
        open = false
        openAnim.start(true)

        Executor(0) {
            if (!openAnim.isAnimating()) destroyExecutor()
            drawScreen(0, 0, 0f)
        }.register()

        for (i in 0 until huds.size) {
            huds[i].hoverHandler.reset()
        }
        Config.save()
    }

    /**
     * Creates an Executor that slowly resets hud's position
     */
    fun resetHUDs() {
        if (resetAnim.start()) {
            for (i in huds) {
                i.resetX = i.x
                i.resetY = i.y
                i.resetScale = i.scale
            }
            Executor(0) {
                if (!resetAnim.isAnimating()) {
                    Config.save()
                    destroyExecutor()
                }
                for (hud in huds) {
                    hud.x = resetAnim.get(hud.resetX, hud.xSetting.default)
                    hud.y = resetAnim.get(hud.resetY, hud.ySetting.default)
                    hud.scale = resetAnim.get(hud.resetScale, hud.scaleSetting.default)
                }
            }.register()
        }
    }
}
