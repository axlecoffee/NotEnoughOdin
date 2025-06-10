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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.LinearAnimation
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.isAreaHovered

class HoverHandler(private val startDelay: Long, delay: Long) {

    constructor(delay: Long) : this(0, delay)

    val anim = LinearAnimation<Float>(delay)

    private var hoverStartTime: Long? = null
    var hasStarted = false

    fun percent(): Int {
        if (!hasStarted) return 100 - anim.getPercent()
        return anim.getPercent()
    }

    fun handle(x: Float, y: Float, w: Float, h: Float) {
        if (isAreaHovered(x, y, w, h)) {
            if (hoverStartTime == null) hoverStartTime = System.currentTimeMillis()

            if (System.currentTimeMillis() - hoverStartTime!! >= startDelay && !hasStarted) {
                anim.start()
                hasStarted = true
            }
        } else {
            hoverStartTime = null
            if (hasStarted) {
                anim.start()
                hasStarted = false
            }
        }
    }

    fun reset() {
        hoverStartTime = null
        hasStarted = false
    }
}
