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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils.isInSkyblock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Module to automatically do the Melody's Harp minigame.
 *
 * Modified from: https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/module/impl/misc/AutoHarp.kt
 *
 * @author Aton, X45k
 */
object AutoHarp : Module(
    name = "Auto Harp",
    desc = "Automatically Completes Melody's Harp."
){
    private var lastInv = 0

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!isInSkyblock) return
        val container = Minecraft.getMinecraft().thePlayer?.openContainer as? ContainerChest ?: return
        if (!container.name.startsWith("Harp -") || container.inventorySlots.size < 54) return
        lastInv = container.inventorySlots.subList(0,36).joinToString("") { it?.stack?.displayName ?: "" }.hashCode().takeIf { lastInv != it } ?: return
        repeat(7) {
            val slot = container.inventorySlots[37 + it]
            if ((slot.stack?.item as? ItemBlock)?.block === Blocks.quartz_block) windowClick(slot.slotNumber, ClickType.Middle)
        }
    }
}
