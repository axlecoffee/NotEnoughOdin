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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.ClickEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object SwapSound : Module(
    name = "Swap Sound",
    desc = "Plays a sound when you successfully stonk swap."
) {
    private val onlyBlock by BooleanSetting("Only Over Block", false, desc = "Only plays a sound when you're looking at a block.")
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, desc = "Which sound to play when you successfully stonk swap.")
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume by NumberSetting("Volume", 1f, 0, 1, .01f, desc = "Volume of the sound.")
    private val pitch by NumberSetting("Pitch", 2f, 0, 2, .01f, desc = "Pitch of the sound.")
    private val reset by ActionSetting("Play sound", desc = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch)
    }

    private var slot: Int? = null
    private val pickaxes = arrayListOf(Items.diamond_pickaxe, Items.golden_pickaxe, Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe)
    private var playedThisTick = false

    init {
        onPacket<C09PacketHeldItemChange> {
            slot = it.slotId
        }
    }

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.Left) {
        if (Minecraft.getMinecraft().thePlayer?.heldItem?.item !in pickaxes || Minecraft.getMinecraft().thePlayer?.inventory?.mainInventory?.get(slot ?: return)?.item in pickaxes || playedThisTick || (onlyBlock && Minecraft.getMinecraft().objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)) return
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch)
        playedThisTick = true
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        playedThisTick = false
    }
}
