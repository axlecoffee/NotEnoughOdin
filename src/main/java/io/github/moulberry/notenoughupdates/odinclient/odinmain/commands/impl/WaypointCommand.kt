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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.WaypointManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component1
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component2
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.floorVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.posX
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.posY
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.posZ
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import kotlin.math.roundToInt

val waypointCommand = Commodore("waypoint", "odinwaypoint") {

    literal("help").runs {
        modMessage(
            """
                 Waypoint command help:
                 §3- /waypoint » §8Main command.
                 §3- /waypoint share » §8Used to send your location in party chat.
                 §3- /waypoint addtemp » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <x, y, z> » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <name, x?, y?, z?> » §8Used to add temporary waypoints.
            """.trimIndent()
        )
    }

    literal("share") {
        runs {
            partyMessage(PlayerUtils.getPositionString())
        }
        runs { x: Int, y: Int, z: Int ->
            partyMessage("x: $x, y: $y, z: $z")
        }
    }

    literal("addtemp") {
        runs { x: Int, y: Int, z: Int ->
            WaypointManager.addTempWaypoint("Waypoint", x, y, z)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val (posX, posY, posZ) = Minecraft.getMinecraft().thePlayer?.positionVector?.floorVec() ?: return@runs
            WaypointManager.addTempWaypoint(name, x ?: posX.toInt(), y ?: posY.toInt(), z ?: posZ.toInt())
        }

        runs {
            WaypointManager.addTempWaypoint("", posX.roundToInt(), posY.roundToInt(), posZ.roundToInt())
        }
    }
}
