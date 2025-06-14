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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.PBConfig
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.getSafe

class PersonalBest(val name: String, val size: Int) {
    var pb get() = PBConfig.pbs[name]
            set(value) { value?.let { PBConfig.pbs[name] = it } }
    init {
        if (pb == null || pb?.size != size) {
            pb = MutableList(size) { 9999.0 }
            PBConfig.saveConfig()
        }
    }

    /**
     * Updates the personal best at the specified index.
     *
     * @param index The index of the personal best to update.
     * @param time The time to compare with the personal best.
     * @param unit The unit of the time.
     * @param message The message to send.
     * @param addPBString Whether to add a string to the message indicating a new personal best.
     * @param addOldPBString Whether to add a string to the message indicating the old personal best.
     * @param sendOnlyPB Whether to only send the message if the time is a new personal best.
     * @param alwaysSendPB Whether to always send the old personal best in the message.
     * @param sendMessage Whether to send the message at all.
     */
    fun time(index: Int, time: Double, unit: String = "s§7!", message: String, addPBString: Boolean, addOldPBString: Boolean, sendOnlyPB: Boolean = false, alwaysSendPB: Boolean = false, sendMessage: Boolean = true) {
        var msg = "$message$time$unit"
        val oldPB = pb?.getSafe(index) ?: 999.0
        if (oldPB > time) {
            set(index, time)
            if (addPBString) msg += " §7(§d§lNew PB§r§7)"
            if (addOldPBString) msg += " Old PB was §8$oldPB"
            if (sendMessage) modMessage(msg)
        } else if (!sendOnlyPB && sendMessage) modMessage("$msg ${if (alwaysSendPB) "(§8$oldPB§7)" else ""}")
    }

    fun set(index: Int, value: Double) {
        pb?.set(index, value)
        PBConfig.saveConfig()
    }
}
