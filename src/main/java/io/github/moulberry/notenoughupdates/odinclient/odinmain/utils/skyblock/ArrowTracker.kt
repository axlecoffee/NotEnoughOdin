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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.ArrowEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PostEntityMetadata
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.*
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S13PacketDestroyEntities
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.Vec3i
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

object ArrowTracker {
    private val ownedArrows = ConcurrentHashMap<Vec3i, OwnedData>()
    private val arrows = ConcurrentHashMap<Int, ArrowData>()

    private var currentTick = 0L

    @SubscribeEvent
    fun onArrowHit(event: ArrowEvent.Hit) {
        if (event.target.isEntityAlive) arrows[event.arrow.entityId]?.entitiesHit?.add(event.target)
    }

    @SubscribeEvent
    fun onMetadata(event: PostEntityMetadata) = with(event.packet) {
        arrows[entityId]?.takeIf { it.arrow == null }?.let {
            val arrow = Minecraft.getMinecraft().theWorld.getEntityByID(entityId) as? EntityArrow ?: return@with

            it.arrow = arrow
            it.owner = findOwner(arrow)
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) = with(event.packet) {
        when (this) {
            is S0EPacketSpawnObject -> if (type == 60) arrows[entityID] = ArrowData()
            is S13PacketDestroyEntities -> entityIDs.forEach {
                arrows.remove(it)?.run {
                    ArrowEvent.Despawn(arrow ?: return@run, owner ?: return@run, entitiesHit).postAndCatch()
                }
            }
            is S32PacketConfirmTransaction -> {
                currentTick++
                ownedArrows.entries.removeAll { currentTick - it.value.addedTime > 12 }
            }
            else -> return@with
        }
    }

    private fun findOwner(packet: EntityArrow): Entity? = with(packet) {
        arrows[entityId]?.owner?.let { return it }
        val arrowPos = Vec3i(serverPosX, serverPosY, serverPosZ)

        shootingEntity?.let {
            ownedArrows[arrowPos] = OwnedData(it, currentTick)
            return it
        }

        return (ownedArrows[arrowPos] ?: ownedArrows[arrowPos.addVec(y=16)])?.owner
    }

    data class OwnedData(val owner: Entity, val addedTime: Long)
    data class ArrowData(var owner: Entity? = null, var arrow: EntityArrow? = null, val entitiesHit: ArrayList<Entity> = ArrayList())
}
