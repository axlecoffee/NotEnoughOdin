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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.TerminalEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.StringSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    desc = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private var claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")

    @SubscribeEvent
    fun onTermLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || Minecraft.getMinecraft().currentScreen is TermSimGUI) return
        if (sendMelodyMessage) partyMessage(melodyMessage)
        if (melodySendCoords) sendCommand("od sendcoords", true)

        claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")
    }

    init {
        execute(250) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || TerminalSolver.currentTerm?.type != TerminalTypes.MELODY || Minecraft.getMinecraft().currentScreen is TermSimGUI || !melodyProgress) return@execute

            val greenClayIndices = claySlots.keys.filter { index -> TerminalSolver.currentTerm?.items?.get(index)?.metadata == 5 }.ifEmpty { return@execute }

            partyMessage(claySlots[greenClayIndices.last()] ?: return@execute)
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}
