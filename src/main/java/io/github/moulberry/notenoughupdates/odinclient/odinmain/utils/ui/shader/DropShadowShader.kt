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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.shader

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.gui.Gui

object DropShadowShader: Shader("/shaders/rectangle.vsh", "/shaders/dropShadow.fsh") {

    private var x = 0f
    private var y = 0f
    private var width = 0f
    private var height = 0f
    private var shadowColor = Colors.WHITE
    private var topL = 0f
    private var topR = 0f
    private var botL = 0f
    private var botR = 0f
    private var shadowSoftness = 0f

    fun drawShadow(
        x: Float, y: Float, width: Float, height: Float,
        shadowColor: Color, topL: Float, topR: Float, botL: Float, botR: Float, shadowSoftness: Float
    ) {
        if (!usable) return

        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.shadowColor = shadowColor
        this.topL = topL
        this.topR = topR
        this.botL = botL
        this.botR = botR
        this.shadowSoftness = shadowSoftness

        startShader()

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), shadowColor.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_rectCenter")
        setupUniform("u_rectSize")
        setupUniform("u_Radii")
        setupUniform("u_colorShadow")
        setupUniform("u_shadowSoftness")
    }

    override fun updateUniforms() {
        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_Radii").setValue(botR, topR, botL, topL)
        getFloat4Uniform("u_colorShadow").setValue(shadowColor.red / 255f, shadowColor.green / 255f, shadowColor.blue / 255f, shadowColor.alphaFloat)
        getFloatUniform("u_shadowSoftness").setValue(shadowSoftness)
    }
}
