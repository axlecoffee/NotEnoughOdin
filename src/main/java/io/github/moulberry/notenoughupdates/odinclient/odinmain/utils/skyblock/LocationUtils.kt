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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.Dungeon
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.startsWithOneOf
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3BPacketScoreboardObjective
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object LocationUtils {
    var isOnHypixel: Boolean = false
        private set
    var isInSkyblock: Boolean = false
        private set
    var currentDungeon: Dungeon? = null
        private set
    var currentArea: Island = Island.Unknown
        private set

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        currentArea = Island.Unknown
        currentDungeon = null
        isInSkyblock = false
        isOnHypixel = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        currentArea = Island.Unknown
        currentDungeon = null
        isInSkyblock = false
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        if (Minecraft.getMinecraft().isSingleplayer) currentArea = Island.SinglePlayer

        isOnHypixel = Minecraft.getMinecraft().runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.contains("hypixel", true) ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S3FPacketCustomPayload -> {
                if (isOnHypixel || event.packet.channelName != "MC|Brand") return
                if (event.packet.bufferData?.readStringFromBuffer(Short.MAX_VALUE.toInt())?.contains("hypixel", true) == true) isOnHypixel = true
            }

            is S38PacketPlayerListItem -> {
                if (!currentArea.isArea(Island.Unknown) || !event.packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) return
                val area = event.packet.entries?.find { it?.displayName?.unformattedText?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.formattedText ?: return

                currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
                if (DungeonUtils.inDungeons && currentDungeon == null) currentDungeon = Dungeon()
            }

            is S3BPacketScoreboardObjective ->
                if (!isInSkyblock)
                    isInSkyblock = isOnHypixel && event.packet.func_149339_c() == "SBScoreboard"
        }
    }
}
