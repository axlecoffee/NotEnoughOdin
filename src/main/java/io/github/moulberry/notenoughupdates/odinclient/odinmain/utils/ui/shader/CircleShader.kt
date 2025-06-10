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

object CircleShader: Shader("/shaders/rectangle.vsh", "/shaders/circleFragment.fsh") {

    private var x = 0f
    private var y = 0f
    private var radius = 0f
    private var color = Colors.WHITE
    private var borderColor = Colors.WHITE
    private var borderThickness = 0f

    fun drawCircle(
        x: Float, y: Float, radius: Float,
        color: Color, borderColor: Color, borderThickness: Float
    ) {
        if (!usable) return

        this.x = x
        this.y = y
        this.radius = radius
        this.color = color
        this.borderColor = borderColor
        this.borderThickness = borderThickness

        startShader()

        Gui.drawRect((x - radius).toInt(), (y - radius).toInt(), (x + radius).toInt(), (y + radius).toInt(), color.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_circleCenter")
        setupUniform("u_circleRadius")
        setupUniform("u_colorCircle")
        setupUniform("u_colorBorder")
        setupUniform("u_borderThickness")
    }

    override fun updateUniforms() {
        getFloat2Uniform("u_circleCenter").setValue(x, y)
        getFloatUniform("u_circleRadius").setValue(radius)
        getFloat4Uniform("u_colorCircle").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.red / 255f, borderColor.green / 255f, borderColor.blue / 255f, borderColor.alphaFloat)
        getFloatUniform("u_borderThickness").setValue(borderThickness)
    }
}
