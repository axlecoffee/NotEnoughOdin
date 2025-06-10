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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RoomEnterEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.isXZInterceptable
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toAABB
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.util.concurrent.CopyOnWriteArraySet

object TPMazeSolver {
    private var tpPads = setOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name == "Teleport Maze") tpPads = endPortalFrameLocations.map { getRealCoords(it.x, it.y, it.z) }.toSet()
    }

    fun tpPacket(event: S08PacketPlayerPosLook) {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || event.x % 0.5 != 0.0 || event.y != 69.5 || event.z % 0.5 != 0.0 || tpPads.isEmpty()) return
        visited.addAll(tpPads.filter { Vec3(event.x, event.y, event.z).toAABB().expand(0.5, 0.0, 0.5).intersectsWith(it.toAABB()) ||
                Minecraft.getMinecraft().thePlayer?.entityBoundingBox?.expand(0.5, 0.0, 0.5)?.intersectsWith(it.toAABB()) == true })
        getCorrectPortals(Vec3(event.x, event.y, event.z), event.yaw, event.pitch)
    }

    private fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(tpPads)

        correctPortals = correctPortals.filter {
            it !in visited &&
            isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                60f, pos, yaw, pitch
            ) && !it.toAABB().expand(.5, .0, .5).isVecInside(Minecraft.getMinecraft().thePlayer?.positionVector)
        }
    }

    fun onRenderWorld(mazeColorOne: Color, mazeColorMultiple: Color, mazeColorVisited: Color) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        tpPads.forEach {
            when (it) {
                in correctPortals -> Renderer.drawBlock(it, if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple, outlineAlpha = 0, depth = false)
                in visited -> Renderer.drawBlock(it, mazeColorVisited, outlineAlpha = 0, depth = true)
                else -> Renderer.drawBlock(it, Colors.WHITE.withAlpha(0.5f), outlineAlpha = 0, fillAlpha = 0.5f, depth = true)
            }
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
    }

    private val endPortalFrameLocations = setOf(
        BlockPos(4, 69, 28), BlockPos(4, 69, 22), BlockPos(4, 69, 20),
        BlockPos(4, 69, 14), BlockPos(4, 69, 12), BlockPos(4, 69, 6),
        BlockPos(10, 69, 28), BlockPos(10, 69, 22), BlockPos(10, 69, 20),
        BlockPos(10, 69, 14), BlockPos(10, 69, 12), BlockPos(10, 69, 6),
        BlockPos(12, 69, 28), BlockPos(12, 69, 22), BlockPos(15, 69, 14),
        BlockPos(15, 69, 12), BlockPos(18, 69, 28), BlockPos(18, 69, 22),
        BlockPos(20, 69, 28), BlockPos(20, 69, 22), BlockPos(20, 69, 20),
        BlockPos(20, 69, 14), BlockPos(20, 69, 12), BlockPos(20, 69, 6),
        BlockPos(26, 69, 28), BlockPos(26, 69, 22), BlockPos(26, 69, 20),
        BlockPos(26, 69, 14), BlockPos(26, 69, 12), BlockPos(26, 69, 6)
    )
}
