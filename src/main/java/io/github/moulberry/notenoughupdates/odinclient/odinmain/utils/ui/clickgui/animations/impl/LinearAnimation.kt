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

class LinearAnimation<E>(duration: Long): Animation<E>(duration) where E : Number, E: Comparable<E> {

    @Suppress("UNCHECKED_CAST")
    override fun get(start: E, end: E, reverse: Boolean): E {
        val startVal = if (reverse) end.toFloat() else start.toFloat()
        val endVal = if (reverse) start.toFloat()  else end.toFloat()

        if (!isAnimating()) return if (reverse) start else end
        return (startVal + (endVal - startVal) * (getPercent() / 100f)) as E
    }
}
