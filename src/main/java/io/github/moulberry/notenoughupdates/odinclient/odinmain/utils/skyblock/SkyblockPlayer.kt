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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object SkyblockPlayer {
    /*
    in module there should be:
    health display current/Max
    health bar
    defense display
    mana display current/Max
    mana bar
    current speed
    current ehp
    current overflow mana
     */

    private val HEALTH_REGEX = Regex("([\\d|,]+)/([\\d|,]+)❤")
    private val MANA_REGEX = Regex("([\\d|,]+)/([\\d|,]+)✎")
    private val OVERFLOW_MANA_REGEX = Regex("([\\d|,]+)ʬ")
    private val DEFENSE_REGEX = Regex("([\\d|,]+)❈ Defense")

    inline val currentHealth: Int get() = (Minecraft.getMinecraft().thePlayer?.let { player -> (maxHealth * player.health / player.maxHealth).toInt() } ?: 0)
    var maxHealth: Int = 0
    var currentMana: Int = 0
    var maxMana: Int = 0
    private var currentSpeed: Int = 0
    var currentDefense: Int = 0
    var overflowMana: Int = 0
    var effectiveHP: Int = 0

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: PacketEvent.Receive) {
        if (event.packet !is S02PacketChat || event.packet.type != 2.toByte()) return
        val msg = event.packet.chatComponent.unformattedText.noControlCodes

        HEALTH_REGEX.find(msg)?.destructured?.let { (_, maxHp) ->
            maxHealth = maxHp.replace(",", "").toIntOrNull() ?: maxHealth
        }

        MANA_REGEX.find(msg)?.destructured?.let { (cMana, mMana) ->
            currentMana = cMana.replace(",", "").toIntOrNull() ?: currentMana
            maxMana = mMana.replace(",", "").toIntOrNull() ?: maxMana
        }

        OVERFLOW_MANA_REGEX.find(msg)?.groupValues?.get(1)?.let {
            overflowMana = it.replace(",", "").toIntOrNull() ?: overflowMana
        }

        DEFENSE_REGEX.find(msg)?.groupValues?.get(1)?.let {
            currentDefense = it.replace(",", "").toIntOrNull() ?: currentDefense
        }

        effectiveHP = (currentHealth * (1 + currentDefense / 100))
        currentSpeed = floor((Minecraft.getMinecraft().thePlayer?.capabilities?.walkSpeed ?: 0f) * 1000f).toInt()
    }
}
