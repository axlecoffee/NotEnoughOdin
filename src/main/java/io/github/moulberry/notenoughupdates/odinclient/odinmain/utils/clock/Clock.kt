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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock

@Suppress("NOTHING_TO_INLINE")
/**
 * Class to simplify handling delays with [System.currentTimeMillis]
 *
 * @see [hasTimePassed]
 * @see [Executor]
 * @author Stivais
 */
class Clock(val delay: Long = 0L) {

    var lastTime = System.currentTimeMillis()

    inline fun getTime(): Long {
        return System.currentTimeMillis() - lastTime
    }

    inline fun setTime(time: Long) {
        lastTime = time
    }

    /**
     * Sets lastTime to now
     */
    inline fun update() {
        lastTime = System.currentTimeMillis()
    }

    inline fun updateCD() {
        lastTime = System.currentTimeMillis() + delay
    }

    /**
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    inline fun timeLeft(): Long {
        return lastTime - System.currentTimeMillis()
    }

    /**
     * @param delay the delay to check if it has passed since lastTime
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(delay: Long, setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}
