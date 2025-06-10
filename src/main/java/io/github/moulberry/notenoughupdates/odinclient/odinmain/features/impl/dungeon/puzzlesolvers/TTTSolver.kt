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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PostEntityMetadata
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RoomEnterEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toAABB
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import kotlin.experimental.and

object TTTSolver {

    // currently just rendering the board, no actual solving

    private var board = Array(9) { index ->
        BoardSlot(
            State.Blank, BlockPos(0, 0, 0), index % 3, index / 3,
            when (index) {
                4 -> BoardPosition.Middle
                0, 2, 6, 8 -> BoardPosition.Corner
                else -> BoardPosition.Edge
            }
        )
    }

    private data class BoardSlot(val state: State, val location: BlockPos, val row: Int, val column: Int, val position: BoardPosition)

    private var toRender: BlockPos? = null

    fun tttRoomEnter(event: RoomEnterEvent) {
        val room = event.room ?: return
        if (room.data.name != "Tic Tac Toe") return

        //updateBoard(room.vec2.addRotationCoords(room.rotation, 7, 0), room.rotation)
    }

//    private fun updateBoard(bottomRight: Vec2, rotations: Rotations) {
//        for (index in 0 until 9) {
//            val currentSlot = bottomRight.addRotationCoords(rotations, 0, -index / 3).let { BlockPos(it.x.toDouble(), 70.0 + index % 3, it.z.toDouble())}
//            board[index] = BoardSlot(findSlotState(currentSlot), currentSlot, index % 3, index / 3,
//                when (index) {
//                    4 -> BoardPosition.Middle
//                    0, 2, 6, 8 -> BoardPosition.Corner
//                    else -> BoardPosition.Edge
//                })
//        }
//    }

    fun onMetaData(event: PostEntityMetadata) {
        val room = DungeonUtils.currentRoom ?: return
        if (room.data.name != "Tic Tac Toe") return

        if (Minecraft.getMinecraft().theWorld?.getEntityByID(event.packet.entityId) !is EntityItemFrame) return
       // updateBoard(room.vec2.addRotationCoords(room.rotation, 7, 0), room.rotation)
    }

    fun tttRenderWorld() {
        board.forEach { slot ->
            val color = when (slot.state) {
                State.X -> Colors.MINECRAFT_RED
                State.O -> Colors.MINECRAFT_BLUE
                else -> Colors.WHITE
            }
            Renderer.drawBox(slot.location.toAABB(), color, 1f, fillAlpha = 0f)
        }
    }



    fun reset() {
        toRender = null
        board = Array(9) { index ->
            BoardSlot(
                State.Blank, BlockPos(0, 0, 0), index % 3, index / 3,
                when (index) {
                    4 -> BoardPosition.Middle
                    0, 2, 6, 8 -> BoardPosition.Corner
                    else -> BoardPosition.Edge
                }
            )
        }
    }

    private fun findSlotState(blockPos: BlockPos): State {
        val itemFrameDisplayItem = Minecraft.getMinecraft().theWorld?.getEntitiesWithinAABB(EntityItemFrame::class.java, blockPos.toAABB())?.firstOrNull()?.displayedItem ?: return State.Blank
        val mapData = Items.filled_map?.getMapData(itemFrameDisplayItem, Minecraft.getMinecraft().theWorld) ?: return State.Blank
        return if ((mapData.colors[8256] and 255.toByte()).toInt() == 114) State.X else State.O
    }

    enum class State {
        Blank, X, O
    }

    enum class BoardPosition {
        Middle, Edge, Corner
    }
}
