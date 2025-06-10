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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CloseChest : Module(
    name = "Close Chest",
    desc = "Allows you to instantly close chests with any key or automatically."
) {
    private val mode by SelectorSetting("Mode", "Auto", arrayListOf("Auto", "Any Key"), desc = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: PacketEvent.Receive) {
        val packet = event.packet as? S2DPacketOpenWindow ?: return
        if (!inDungeons || !packet.windowTitle.unformattedText.noControlCodes.equalsOneOf("Chest", "Large Chest") || mode != 0) return
        Minecraft.getMinecraft().netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiEvent.KeyPress) {
        if (!inDungeons || mode != 1) return
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (gui.name.noControlCodes.equalsOneOf("Chest", "Large Chest")) Minecraft.getMinecraft().thePlayer?.closeScreen()
    }

    @SubscribeEvent
    fun onMouse(event: GuiEvent.MouseClick) {
        if (!inDungeons || mode != 1) return
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (gui.name.noControlCodes.equalsOneOf("Chest", "Large Chest")) Minecraft.getMinecraft().thePlayer?.closeScreen()
    }
}
