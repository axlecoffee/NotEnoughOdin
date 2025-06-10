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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.runIn
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand

object DungeonRequeue : Module(
    name = "Dungeon Requeue",
    desc = "Automatically starts a new dungeon at the end of a dungeon."
) {
    private val delay by NumberSetting("Delay", 2, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s")
    private val type by BooleanSetting("Type", true, desc = "The type of command to execute to fulfill the requeue request. (true for Normal, false for Requeue)")
    private val disablePartyLeave by BooleanSetting("Disable on leave/kick", true, desc = "Disables the requeue on party leave message.")

    var disableRequeue = false
    init {
        onMessage(Regex(" {29}> EXTRA STATS <")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                if (!disableRequeue)
                    sendCommand(if (type) "instancerequeue" else "od ${DungeonUtils.floor?.name?.lowercase()}", clientSide = !type)
            }
        }

        onMessage(Regex("(\\[.+])? ?(.{1,16}) has (left|been removed from) the party.")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("The party was transferred to (\\[.+])? ?(.{1,16}) because (\\[.+])? ?(.{1,16}) left")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("The party was disbanded because all invites expired and the party was empty.")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("Kicked (\\[.+])? ?(.{1,16}) because they were offline.")) {
            if (disablePartyLeave) disableRequeue = true
        }

        onMessage(Regex("You have been kicked from the party by (\\[.+])? ?(.{1,16})")) {
            disableRequeue = true
        }
        onMessage(Regex("You left the party.")) {
            disableRequeue = true
        }
        onMessage(Regex("(\\[.+])? ?(.{1,16}) has disbanded the party.")) {
            disableRequeue = true
        }

        onWorldLoad { disableRequeue = false }
    }
}
