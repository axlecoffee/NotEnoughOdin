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

class EaseOutAnimation(duration: Long): Animation<Float>(duration) {

    override fun get(start: Float, end: Float, reverse: Boolean): Float {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end
        if (!isAnimating()) return endVal
        return startVal + (endVal - startVal) * easeOutQuad()
    }

    private fun easeOutQuad(): Float {
        val percent = getPercent() / 100f
        return 1 - (1 - percent) * (1 - percent)
    }
}
