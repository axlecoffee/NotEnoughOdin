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

import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20

class FloatUniform(private val location: Int) {
    fun setValue(value: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform1f(location, value)
        else ARBShaderObjects.glUniform1fARB(location, value)
    }
}

class Float2Uniform(private val location: Int) {
    fun setValue(x: Float, y: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform2f(location, x, y)
        else ARBShaderObjects.glUniform2fARB(location, x, y)
    }
}

class Float4Uniform(private val location: Int) {
    fun setValue(x: Float, y: Float, z: Float, w: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform4f(location, x, y, z, w)
        else ARBShaderObjects.glUniform4fARB(location, x, y, z, w)
    }
}
