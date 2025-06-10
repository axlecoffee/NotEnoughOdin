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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.Animation
import kotlin.math.pow

class EaseInOut(duration: Long): Animation<Float>(duration) {
    override fun get(start: Float, end: Float, reverse: Boolean): Float {
        if (!isAnimating()) return if (reverse) start else end
        return if (reverse) end + (start - end) * easeInOutCubic() else start + (end - start) * easeInOutCubic()
    }

    private fun easeInOutCubic(): Float {
        val x = getPercent() / 100f
        return if (x < 0.5) { 4 * x * x * x } else { 1 - (-2 * x + 2).pow(3) / 2 }
    }
}
