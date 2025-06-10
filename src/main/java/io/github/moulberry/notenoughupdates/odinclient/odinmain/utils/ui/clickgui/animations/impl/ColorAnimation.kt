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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color

// TODO: fix it
class ColorAnimation(duration: Long) {

    private val anim = LinearAnimation<Int>(duration) // temporary fix to weird colors

    fun start(bypass: Boolean = false): Boolean {
        return anim.start(bypass)
    }

    fun isAnimating(): Boolean {
        return anim.isAnimating()
    }

    fun percent(): Int {
        return anim.getPercent()
    }

    fun get(start: Color, end: Color, reverse: Boolean): Color {
        return Color(
            anim.get(start.red, end.red, reverse),
            anim.get(start.green, end.green, reverse),
            anim.get(start.blue, end.blue, reverse),
            anim.get(start.alpha, end.alpha, reverse) / 255f,
        )
    }

    /*
    override fun get(start: Color, end: Color, reverse: Boolean): Color {
        if (!isAnimating()) return if (reverse) start else end
        return Color(
            calculate(start.red, end.red, reverse),
            calculate(start.green, end.green, reverse),
            calculate(start.blue, end.blue, reverse),
        )
    }

    private fun calculate(start: Int, end: Int, reverse: Boolean) =
        ((if (reverse) end + (start - end) else start + (end - start)) * getPercent() / 100f).toInt().coerceIn(0, 255)

     */
}
