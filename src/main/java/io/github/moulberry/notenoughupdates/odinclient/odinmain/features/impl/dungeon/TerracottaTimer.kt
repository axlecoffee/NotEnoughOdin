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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.BlockChangeEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equal
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toFixed
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toVec3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    desc = "Displays the time until the terracotta respawns."
) {
    private var terracottaSpawning = CopyOnWriteArrayList<Terracotta>()
    private data class Terracotta(val pos: Vec3, var time: Float)

    init {
        onPacket<S32PacketConfirmTransaction> {
            terracottaSpawning.removeAll {
                it.time -= .05f
                it.time <= 0
            }
        }
    }

    @SubscribeEvent
    fun onBlockPacket(event: BlockChangeEvent) {
        if (DungeonUtils.isFloor(6) && DungeonUtils.inBoss && event.updated.block.isFlowerPot && terracottaSpawning.none { it.pos.equal(event.pos.toVec3().addVec(0.5, 1.5, 0.5)) })
            terracottaSpawning.add(Terracotta(event.pos.toVec3().addVec(0.5, 1.5, 0.5), if (DungeonUtils.floor?.isMM == true) 12f else 15f))
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(6) || terracottaSpawning.isEmpty()) return
        terracottaSpawning.forEach {
            Renderer.drawStringInWorld("${it.time.toFixed()}s", it.pos, getColor(it.time), depth = false, scale = 0.03f)
        }
    }

    private fun getColor(time: Float): Color {
        return when {
            time > 5f -> Colors.MINECRAFT_DARK_GREEN
            time > 2f -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_DARK_RED
        }
    }
}
