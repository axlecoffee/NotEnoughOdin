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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock

/**
 * Simple class that calculates a "point" between two values and a percentage.
 * @author Stivais
 */
abstract class Animation<T>(private var duration: Long) {

    private var animating = false
    private val clock = Clock(duration)

    fun start(bypass: Boolean = false): Boolean {
        if (!animating || bypass) {
            animating = true
            clock.update()
            return true
        }
        return false
    }

    fun getPercent(): Int {
        return if (animating) {
            val percent = (clock.getTime() / duration.toDouble() * 100).toInt()
            if (percent > 100) animating = false
            percent
        } else {
            100
        }
    }

    fun isAnimating(): Boolean {
        return animating
    }

    abstract fun get(start: T, end: T, reverse: Boolean = false): T
}
