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

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RoomEnterEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addRotationCoords
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.removeFirstOrNull
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BoulderSolver {
    private data class BoxPosition(val render: BlockPos, val click: BlockPos)
    private var currentPositions = mutableListOf<BoxPosition>()
    private var solutions: Map<String, List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/boulderSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            solutions = gson.fromJson(text, object : TypeToken<Map<String, List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            solutions = emptyMap()
        }
    }

    fun onRoomEnter(event: RoomEnterEvent) {
        val room = event.room ?: return reset()
        if (room.data.name != "Boulder") return reset()
        val roomComponent = room.roomComponents.firstOrNull() ?: return reset()
        var str = ""
        for (z in -3..2) {
            for (x in -3..3) {
                roomComponent.blockPos.addRotationCoords(room.rotation, x * 3, z * 3).let { str += if (getBlockIdAt(it.down(4)) == 0) "0" else "1" }
            }
        }
        currentPositions = solutions[str]?.map { sol ->
            val render = roomComponent.blockPos.addRotationCoords(room.rotation, sol[0], sol[1]).down(5)
            val click = roomComponent.blockPos.addRotationCoords(room.rotation, sol[2], sol[3]).down(5)
            BoxPosition(render, click)
        }?.toMutableList() ?: return
    }

    fun onRenderWorld(showAllBoulderClicks: Boolean, boulderStyle: Int, boulderColor: Color, boulderLineWidth: Float) {
        if (DungeonUtils.currentRoomName != "Boulder" || currentPositions.isEmpty()) return
        if (showAllBoulderClicks) currentPositions.forEach {
            Renderer.drawStyledBlock(it.render, boulderColor, boulderStyle, boulderLineWidth)
        } else currentPositions.firstOrNull()?.let {
            Renderer.drawStyledBlock(it.render, boulderColor, boulderStyle, boulderLineWidth)
        }
    }

    fun playerInteract(event: C08PacketPlayerBlockPlacement) {
        if (getBlockIdAt(event.position).equalsOneOf(77, 323))
            currentPositions.removeFirstOrNull { it.click == event.position }
    }

    fun reset() {
        currentPositions = mutableListOf()
    }
}
