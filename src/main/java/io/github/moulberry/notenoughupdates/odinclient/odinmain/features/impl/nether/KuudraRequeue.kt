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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.runIn
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand

object KuudraRequeue : Module(
    name = "Kuudra Requeue",
    desc = "Automatically starts a new kuudra at the end of a kuudra."
) {
    private val delay by NumberSetting("Delay", 10, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s")
    private val disablePartyLeave by BooleanSetting("Disable Party Leave", false, desc = "Disables the requeue on party leave message.")

    init {
        onMessage(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!\$")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                if (!disableRequeue) sendCommand("od t${KuudraUtils.kuudraTier}", true)
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
    }
}
