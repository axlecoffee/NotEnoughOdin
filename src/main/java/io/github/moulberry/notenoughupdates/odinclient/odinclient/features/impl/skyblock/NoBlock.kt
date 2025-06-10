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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.hasAbility
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object NoBlock : Module(
    name = "No Block",
    desc = "Prevents you from blocking with items that have an ability, this is effectively NoSlow."
) {
    private val onlyBoss by BooleanSetting("Only Boss", false, desc = "Only prevent blocking in boss fights.")
    private var isRightClickKeyDown = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationUtils.isInSkyblock) return
        isRightClickKeyDown = Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!LocationUtils.isInSkyblock || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR || (onlyBoss && !DungeonUtils.inBoss)) return

        if (Minecraft.getMinecraft().thePlayer?.heldItem?.hasAbility == false || Minecraft.getMinecraft().thePlayer?.heldItem?.item !is ItemSword) return
        event.isCanceled = true

        if (!isRightClickKeyDown)
            Minecraft.getMinecraft().netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(Minecraft.getMinecraft().thePlayer?.heldItem))
    }
}
