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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.DropdownSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.EtherWarpHelper
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard

object Waypoints : Module(
    name = "Waypoints",
    desc = "Allows to render waypoints based on coordinates in chat."
) {
    private val fromParty by BooleanSetting("From Party Chat", true, desc = "Adds waypoints from party chat.")
    private val fromAll by BooleanSetting("From All Chat", false, desc = "Adds waypoints from all chat.")

    private val pingLocationDropDown by DropdownSetting("Ping Location Dropdown", false)
    private val pingLocationToggle by BooleanSetting("Ping Location", false, desc = "Adds a waypoint at the location you are looking at.").withDependency { pingLocationDropDown }
    private val pingLocation by KeybindSetting("Ping Location Keybind", Keyboard.KEY_NONE, description = "Sends the location you are looking at as coords in chat for waypoints.").onPress {
        if (!pingLocationToggle) return@onPress
        EtherWarpHelper.getEtherPos(PositionLook(Minecraft.getMinecraft().thePlayer.renderVec, Minecraft.getMinecraft().thePlayer.rotationYaw, Minecraft.getMinecraft().thePlayer.rotationPitch), pingDistance).pos?.let { pos ->
            val (x, y, z) = pos.addVec(0.5, 0.5, 0.5)
            WaypointManager.addTempWaypoint(x = x, y = y, z = z, time = pingWaypointTime)
            if (sendPingedLocation) sendCommand("odinwaypoint share ${pos.x} ${pos.y} ${pos.z}", true)
        }
    }.withDependency { pingLocationToggle && pingLocationDropDown }
    private val sendPingedLocation by BooleanSetting("Send Pinged Location", false, desc = "Sends the location you are looking at as coords in chat for waypoints.").withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingWaypointTime by NumberSetting("Ping Waypoint Time", 15000L, 0L, 128000L, 1000L, unit = "ms", desc = "Time to wait before sending the waypoint command.").withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingDistance by NumberSetting("Ping Distance", 64.0, 1, 128, 1, desc = "Distance to ping location.").withDependency { pingLocationToggle && pingLocationDropDown }

    init {
        onMessage(Regex("^Party > (?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*"), { fromParty && enabled }) { // https://regex101.com/r/8K26A1/1
            val (name, x, y, z) = it.destructured
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }

        onMessage(Regex("^(?!Party >).*\\s(?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+).*"), { fromAll && enabled }) { // https://regex101.com/r/A3aoyL/1
            val (name, x, y, z) = it.destructured
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }
    }
}
