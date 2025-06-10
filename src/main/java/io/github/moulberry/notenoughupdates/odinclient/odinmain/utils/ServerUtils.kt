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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils


import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S37PacketStatistics
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerUtils {
    private val packets = ArrayList<Packet<*>>()

    @JvmStatic
    fun handleSendPacket(packet: Packet<*>): Boolean {
        return packets.remove(packet)
    }

    private fun sendPacketNoEvent(packet: Packet<*>) {
        packets.add(packet)
        Minecraft.getMinecraft().netHandler?.addToSendQueue(packet)
    }

    private var prevTime = 0L
    var averageTps = 20.0
    var averagePing = 0.0
    private var isPinging = false
    private var pingStartTime = 0L

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        reset()
    }

    init {
        Executor(2000, "ServerUtils") {
            sendPing()
        }.register()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S37PacketStatistics -> averagePing = (System.nanoTime() - pingStartTime) / 1e6

            is S01PacketJoinGame -> averagePing = 0.0

            is S03PacketTimeUpdate -> {
                if (prevTime != 0L)
                    averageTps = (20_000.0 / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0.0, 20.0)

                prevTime = System.currentTimeMillis()
            }
            else -> return
        }
        isPinging = false
    }

    private fun sendPing() {
        if (isPinging || Minecraft.getMinecraft().thePlayer == null) return
        if (pingStartTime - System.nanoTime() > 10e6) reset()
        pingStartTime = System.nanoTime()
        isPinging = true
        sendPacketNoEvent(C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS))
    }

    private fun reset() {
        prevTime = 0L
        averageTps = 20.0
        averagePing = 0.0
    }
}
