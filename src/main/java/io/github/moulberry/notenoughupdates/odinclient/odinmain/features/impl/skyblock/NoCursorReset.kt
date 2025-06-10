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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoCursorReset : Module(
    name = "No Cursor Reset",
    desc = "Makes your cursor stop resetting between guis.",
) {
    private val clock = Clock(150)
    private var wasNotNull = false

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        val oldGuiScreen = Minecraft.getMinecraft().currentScreen
        if (event.gui is GuiChest && (oldGuiScreen is GuiContainer || oldGuiScreen == null)) clock.update()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        wasNotNull = Minecraft.getMinecraft().currentScreen != null
    }

    @JvmStatic
    fun shouldHookMouse(): Boolean {
        return !clock.hasTimePassed() && wasNotNull && enabled
    }
}
