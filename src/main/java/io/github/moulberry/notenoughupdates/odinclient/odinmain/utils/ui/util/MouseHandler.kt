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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util

/**
 * Edited verison of OneConfig InputHandler to have a translation option cuz why not
 * Add more stuff here and maybe merge with the nanoVG stuff so its nice and clean
 * // TODO: Instead of a class implement methods
 */
class MouseHandler {
    private var scaleX = 1f
    private var scaleY = 1f
    private var tX = 0f
    private var tY = 0f

    fun scale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
    }

    fun translate(x: Float, y: Float) {
        tX = x
        tY = y
    }

    val mouseX get() =
        (MouseUtils.mouseX - tX) / scaleX

    val mouseY get() =
        (MouseUtils.mouseY - tY) / scaleY

    fun isAreaHovered(x: Float, y: Float, width: Float, height: Float): Boolean =
        mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height
}
