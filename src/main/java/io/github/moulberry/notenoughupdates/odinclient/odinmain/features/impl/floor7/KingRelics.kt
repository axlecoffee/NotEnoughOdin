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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.relicAnnounceTime
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.selected
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PersonalBest
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.getBlockAt
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.skyblockID
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors

import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.Vec3

object KingRelics {
    private var currentRelic = Relic.None

    enum class Relic (
        val id: String,
        val colorCode: Char,
        val color: Color,
        val spawnPosition: Vec3,
        val cauldronPosition: Vec3
    ) {
        Green("GREEN_KING_RELIC", 'a', Colors.MINECRAFT_GREEN, Vec3(20.5, 6.5, 94.5), Vec3(49.0, 7.0, 44.0)),
        Purple("PURPLE_KING_RELIC", '5', Colors.MINECRAFT_DARK_PURPLE, Vec3(56.5, 8.5, 132.5), Vec3(54.0, 7.0, 41.0)),
        Blue("BLUE_KING_RELIC", 'b', Colors.MINECRAFT_BLUE, Vec3(91.5, 6.5, 94.5), Vec3(59.0, 7.0, 44.0)) ,
        Orange("ORANGE_KING_RELIC", '6', Colors.MINECRAFT_GOLD, Vec3(90.5, 6.5, 56.5), Vec3(57.0, 7.0, 42.0)),
        Red("RED_KING_RELIC", 'c', Colors.MINECRAFT_RED, Vec3(22.5, 6.5, 59.5), Vec3(51.0, 7.0, 42.0)),
        None("", 'f', Colors.WHITE, Vec3(0.0, 0.0, 0.0), Vec3(0.0, 0.0, 0.0))
    }

    private val relicPBs = PersonalBest("Relics", 5)
    private var relicPlaceTimer = 0L
    var relicTicksToSpawn = 0

    @OptIn(ExperimentalStdlibApi::class)
    fun relicsOnMessage(){
        if (WitherDragons.relicAnnounce) partyMessage("${Relic.entries[selected]} Relic")
        relicPlaceTimer = System.currentTimeMillis()
        relicTicksToSpawn = WitherDragons.relicSpawnTicks
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun relicsBlockPlace(packet: C08PacketPlayerBlockPlacement) {
        if (relicPlaceTimer == 0L || !getBlockAt(packet.position).equalsOneOf(Blocks.cauldron, Blocks.anvil)) return

        Relic.entries.find { it.id == currentRelic.id }?.let {
            if (it == Relic.None) return
            relicPBs.time(it.ordinal, (System.currentTimeMillis() - relicPlaceTimer) / 1000.0, "s§7!", "§${it.colorCode}${it.name} relic §7took §6", addPBString = true, addOldPBString = true, sendOnlyPB = false, sendMessage = relicAnnounceTime)
            relicPlaceTimer = 0L
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun relicsOnWorldLast() {
        Relic.entries.forEach {
            if (it != Relic.None && currentRelic.id == it.id)
                Renderer.drawCustomBeacon("", it.cauldronPosition, it.color, distance = false)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun onServerTick() {
        relicTicksToSpawn = (relicTicksToSpawn - 1).coerceAtLeast(0)
        currentRelic = Relic.entries.find { Minecraft.getMinecraft().thePlayer?.inventory?.mainInventory?.any { item -> item.skyblockID == it.id } == true } ?: Relic.None
    }
}
