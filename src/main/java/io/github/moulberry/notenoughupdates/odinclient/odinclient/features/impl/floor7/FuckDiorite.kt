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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ActionSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.profile
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.Island
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.isFloor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase

object FuckDiorite : Module(
    name = "Fuck Diorite",
    desc = "Replaces the pillars in the storm fight with glass."
) {
    private val GLASS_STATE = Blocks.glass.defaultState
    private val STAINED_GLASS = Blocks.stained_glass
    private val GLASS = Blocks.glass
    private val STONE = Blocks.stone
    private val glassStates = Array(16) { STAINED_GLASS.getStateFromMeta(it) }

    private val pillarBasedColor by BooleanSetting("Pillar Based", true, desc = "Swaps the diorite in the pillar to a corresponding color.").withDependency { !schitzo }
    private val colorIndex by SelectorSetting("Color", "None", arrayListOf("NONE", "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK",
            "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"), desc = "Color for the stained glass.").withDependency { !pillarBasedColor && !schitzo }
    private val schitzo by BooleanSetting("Schitzo mode", false, desc = "Schtizoing.")
    private val action by ActionSetting("Force Glass", desc = "Replaces all pillars with glass.") {
        if ((DungeonUtils.inBoss && isFloor(7)) || LocationUtils.currentArea.isArea(Island.SinglePlayer)) replaceDiorite(true)
        else modMessage("§cYou must be in F7/M7 boss room to use this feature.")
    }

    private val pillars = arrayOf(BlockPos(46, 169, 41), BlockPos(46, 169, 65), BlockPos(100, 169, 65), BlockPos(100, 169, 41))
    private val pillarColors = intArrayOf(5, 4, 10, 14)

    private val coordinates: Array<Set<BlockPos>> = Array(4) { pillarIndex ->
        val pillar = pillars[pillarIndex]
        buildSet {
            for (dx in (pillar.x - 3)..(pillar.x + 3))
                for (dy in pillar.y..(pillar.y + 37))
                    for (dz in (pillar.z - 3)..(pillar.z + 3))
                        add(BlockPos(dx, dy, dz))
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == Phase.END && DungeonUtils.getF7Phase() == M7Phases.P2) profile("Diorite Fucker") { replaceDiorite() }
    }

    private fun replaceDiorite(force: Boolean = false) {
        val chunkProvider = Minecraft.getMinecraft().theWorld?.chunkProvider ?: return
        val chunks = mutableMapOf<Long, Chunk>()

        for (coordinate in coordinates) {
            for (pos in coordinate) {
                val chunkX = pos.x shr 4
                val chunkZ = pos.z shr 4

                val chunk = chunks.getOrPut((chunkX.toLong() shl 32) or chunkZ.toLong()) { chunkProvider.provideChunk(chunkX, chunkZ) }
                if (chunk.getBlock(pos) == STONE || (force && chunk.getBlock(pos).equalsOneOf(STONE, GLASS, STAINED_GLASS))) setGlass(pos, coordinate)
            }
        }
    }

    private fun setGlass(pos: BlockPos, coordinate: Set<BlockPos>) {
        Minecraft.getMinecraft().theWorld?.setBlockState(pos, when { // cant use chunk.setBlockState due to it not updating the block
            schitzo -> glassStates.random()
            pillarBasedColor -> glassStates[pillarColors[coordinates.indexOfFirst { coordinate === it }]]
            colorIndex != 0 -> glassStates[colorIndex - 1]
            else -> GLASS_STATE
        }, 3)
    }
}
