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

object HSBBoxShader: Shader("/shaders/rectangle.vsh", "/shaders/hsbbox.fsh") {

    private var x = 0f
    private var y = 0f
    private var width = 0f
    private var height = 0f
    private var color = Colors.WHITE

    fun drawHSBBox(x: Float, y: Float, width: Float, height: Float, color: Color) {
        if (!usable) return

        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.color = color

        startShader()

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_rectCenter")
        setupUniform("u_rectSize")
        setupUniform("u_colorRect")
    }

    override fun updateUniforms() {
        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_colorRect").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
    }
}
