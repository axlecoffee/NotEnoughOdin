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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.terminalhandler

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.TerminalEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalTypes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.postAndCatch
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

open class TerminalHandler(val type: TerminalTypes, val timeOpened: Long = System.currentTimeMillis()) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val items: Array<ItemStack?> = arrayOfNulls(type.windowSize)
    var isClicked: Boolean = false

    @SubscribeEvent(receiveCanceled = true)
    fun onPacketReceived(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is S2FPacketSetSlot -> {
                if (func_149173_d() !in 0 until type.windowSize) return@with
                items[func_149173_d()] = func_149174_e()
                if (handleSlotUpdate(this)) TerminalEvent.Updated(this@TerminalHandler).postAndCatch()
            }
            is S2DPacketOpenWindow -> {
                items.fill(null)
                isClicked = false
            }
        }
    }

    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean = false

    open fun simulateClick(slotIndex: Int, clickType: ClickType) {}

    fun click(slotIndex: Int, clickType: ClickType, simulateClick: Boolean = true) {
        if (simulateClick) simulateClick(slotIndex, clickType)
        isClicked = true
        PlayerUtils.windowClick(slotIndex, clickType)
    }

    fun canClick(slotIndex: Int, button: Int, needed: Int = solution.count { it == slotIndex }): Boolean = when {
        type == TerminalTypes.MELODY -> slotIndex.equalsOneOf(16, 25, 34, 43)
        slotIndex !in solution -> false
        type == TerminalTypes.NUMBERS && slotIndex != solution.firstOrNull() -> false
        type == TerminalTypes.RUBIX && ((needed < 3 && button == 1) || (needed.equalsOneOf(3, 4) && button != 1)) -> false
        else -> true
    }
}
