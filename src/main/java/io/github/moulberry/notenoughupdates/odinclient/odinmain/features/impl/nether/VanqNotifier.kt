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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.allMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage

object VanqNotifier: Module(
    name = "Vanq Notifier",
    desc = "Notifies you when a vanquisher is nearby."
) {
    private val playSound by BooleanSetting("Play Sound", true, desc = "Plays a sound when a vanquisher spawns.")
    private val showText by BooleanSetting("Show Text", true, desc = "Shows a message when a vanquisher spawns.")
    private val ac by BooleanSetting("All Chat", false, desc = "Sends the message to all chat.")
    private val pc by BooleanSetting("Party Chat", true, desc = "Sends the message to party chat.")

   init {
       onMessage(Regex("A Vanquisher is spawning nearby!")) {
           PlayerUtils.alert("§5Vanquisher", playSound = playSound, displayText = showText)
           if (pc) partyMessage(PlayerUtils.getPositionString())
           if (ac) allMessage(PlayerUtils.getPositionString())
           modMessage("§2Vanquisher has spawned!")
       }
   }
}
