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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.puzzlesolvers

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.middle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer.HighlightEntity
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderBoundingBox
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.Puzzle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.PuzzleStatus
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import kotlin.collections.set

object BlazeSolver {
    private var blazes = mutableListOf<EntityBlaze>()
    private var roomType = 0
    private var lastBlazeCount = 10
    private val blazeHealthRegex = Regex("^\\[Lv15] Blaze [\\d,]+/([\\d,]+)❤$")

    fun getBlaze() {
        val room = DungeonUtils.currentRoom ?: return
        if (!DungeonUtils.inDungeons || !room.data.name.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        val hpMap = mutableMapOf<EntityBlaze, Int>()
        blazes.clear()
        Minecraft.getMinecraft().theWorld?.loadedEntityList?.forEach { entity ->
            if (entity !is EntityArmorStand) return@forEach
            val blaze = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
                .filterIsInstance<EntityBlaze>().firstOrNull() ?: return@forEach
            hpMap[blaze] = blazeHealthRegex.find(entity.name.noControlCodes)?.groups?.get(1)?.value?.replace(",", "")?.toIntOrNull() ?: return@forEach
            if (blaze !in blazes) blazes.add(blaze)
        }
        if (room.data.name == "Lower Blaze") blazes.sortByDescending { hpMap[it] }
        else blazes.sortBy { hpMap[it] }
    }

    fun getHighlightedBlazes(blazeFirstColor: Color, blazeSecondColor: Color, blazeAllColor: Color, thickness: Float, depth: Boolean, boxStyle: Int): Collection<HighlightEntity> {
        return blazes.mapIndexed { index, blaze ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                else -> blazeAllColor
            }
            HighlightEntity(blaze, color, thickness, depth, boxStyle)
        }
    }

    fun onRenderWorld(blazeLineNext: Boolean, blazeLineAmount: Int, blazeFirstColor: Color, blazeSecondColor: Color, blazeAllColor: Color, blazeSendComplete: Boolean, blazeLineWidth: Float) {
        if (!DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        if (blazes.isEmpty()) return
        blazes.removeAll { Minecraft.getMinecraft().theWorld?.getEntityByID(it.entityId) == null }
        if (blazes.isEmpty() && lastBlazeCount == 1) {
            LocationUtils.currentDungeon?.puzzles?.find { it == Puzzle.BLAZE }?.status = PuzzleStatus.Completed
            onPuzzleComplete(if (DungeonUtils.currentRoomName == "Higher Blaze") "Higher Blaze" else "Lower Blaze")
            if (blazeSendComplete) partyMessage("Blaze puzzle solved!")
            lastBlazeCount = 0
            return
        }
        lastBlazeCount = blazes.size
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                else -> blazeAllColor
            }

            if (blazeLineNext && index > 0 && index <= blazeLineAmount)
                Renderer.draw3DLine(listOf(blazes[index - 1].renderBoundingBox.middle, entity.renderBoundingBox.middle), color = color, lineWidth = blazeLineWidth, depth = true)
        }
    }

    fun reset() {
        lastBlazeCount = 10
        blazes.clear()
        roomType = 0
    }
}
